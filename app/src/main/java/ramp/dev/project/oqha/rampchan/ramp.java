package ramp.dev.project.oqha.rampchan;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static ramp.dev.project.oqha.rampchan.inpohMemory.getAvailableExternalMemorySize;

public class ramp extends Activity {


   //dropLib Helper
    final static String TARGET_BASE_PATH = Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/";
    private void copyFileOrDir(String path) {
        AssetManager assetManager = this.getAssets();
        String assets[] = null;
        try {
            Log.i("Gagal", "copyFileOrDir() "+path);
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(path);
            } else {
                String fullPath =  TARGET_BASE_PATH + path;
                Log.i("Gagal", "path="+fullPath);
                File dir = new File(fullPath);
                if (!dir.exists() && !path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
                    if (!dir.mkdirs())
                        Log.i("Gagal", "could not create dir "+fullPath);
                for (int i = 0; i < assets.length; ++i) {
                    String p;
                    if (path.equals(""))
                        p = "";
                    else
                        p = path + "/";

                    if (!path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
                        copyFileOrDir( p + assets[i]);
                }
            }
        } catch (IOException ex) {
            Log.e("Gagal", "I/O Exception", ex);
        }
    }
    private void copyFile(String filename) {
        AssetManager assetManager = this.getAssets();

        InputStream in = null;
        OutputStream out = null;
        String newFileName = null;
        try {
            Log.i("Gagal", "copyFile() "+filename);
            in = assetManager.open(filename);
            if (filename.endsWith(".jpg")) // extension was added to avoid compression on APK file
                newFileName = TARGET_BASE_PATH + filename.substring(0, filename.length()-4);
            else
                newFileName = TARGET_BASE_PATH + filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e("Gagal", "Exception in copyFile() of "+newFileName);
            Log.e("Gagal", "Exception in copyFile() "+e.toString());
        }

    }

    //zip Helper
    public void zipFolder(String srcFolder, String destZipFile) throws Exception {
        ZipOutputStream zip = null;
        FileOutputStream fileWriter = null;
        fileWriter = new FileOutputStream(destZipFile);
        zip = new ZipOutputStream(fileWriter);
        addFolderToZip("", srcFolder, zip);
        zip.flush();
        zip.close();
    }
    private void addFileToZip(String path, String srcFile,ZipOutputStream zip) throws Exception {
        File folder = new File(srcFile);
        if (folder.isDirectory()) {
            addFolderToZip(path, srcFile, zip);
        } else {
            byte[] buf = new byte[1024];
            int len;
            FileInputStream in = new FileInputStream(srcFile);
            zip.putNextEntry(new ZipEntry(path.replace("ramp/", "") + "/" + folder.getName()));
            //zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
            while ((len = in.read(buf)) > 0) {
                zip.write(buf, 0, len);
            }
        }
    }
    private void addFolderToZip(String path, String srcFolder,ZipOutputStream zip) throws Exception {
        File folder = new File(srcFolder);
        for (String fileName : folder.list()) {
            if (path.equals("")) {
                addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
            } else {
                addFileToZip(path + "/" + folder.getName(), srcFolder + "/"
                        + fileName, zip);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ramp);
        File dir = new File(Environment.getExternalStorageDirectory() + "/OqhaProject");
        File dir2 = new File(Environment.getExternalStorageDirectory() + "/OqhaProject/ramp");
        if(!dir.exists()) {
            dir.mkdir();
        }
        if(!dir2.exists()) {
            dir2.mkdir();
        }

        Button btn = (Button) findViewById(R.id.Start_button);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                run();
            }
        });



    }
    public void run() {
        AsyncTask task = new core(this).execute();
    }
    public void end(String a) {
        TextView progresT = (TextView)findViewById(R.id.Progres);
        progresT.setText(a);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ramp, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    class core extends AsyncTask<Integer, Integer, String>{
        private Activity activity;
        private ProgressDialog dialog;
        private Context context;
        private String notice;
        //core
        public void root(){
            Log.e("Gagal", "root");

            Process p;
            try {
                p = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(p.getOutputStream());
                os.writeBytes("exit\n");
                os.flush();
                try {
                    p.waitFor();
                    if (p.exitValue() != 255) {
                        cekRoot();
                    }
                    else {
                    }
                } catch (InterruptedException e) {
                }
            } catch (IOException e) {
            }

        }
        public void cekRoot(){
            Log.e("Gagal", "cekRoot");

            Process p;
            try {
                p = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(p.getOutputStream());
                File a = new File(Environment.getExternalStorageDirectory()+"/OqhaProject");
                if (!a.exists()) {
                    os.writeBytes("mkdir " + Environment.getExternalStorageDirectory() + "/OqhaProject\n");
                }
                File b = new File(Environment.getExternalStorageDirectory()+"/OqhaProject/ramp");
                if (!b.exists()) {
                    os.writeBytes("mkdir " + Environment.getExternalStorageDirectory() + "/OqhaProject/ramp\n");
                }

                os.writeBytes("exit\n");
                os.flush();
                try {
                    p.waitFor();
                    if (p.exitValue() != 255) {
                        if (b.exists()){
                            cekFree();
                        }
                        else {
                            Log.e("Gagal","cekRoot error");
                            notice = "Gagal mendapatkan akses Root";
                        }

                    }
                    else {
                    }
                } catch (InterruptedException e) {
                }
            } catch (IOException e) {
            }

        }
        public void cekFree() {
            Log.e("Gagal", "cek free");
            String free = getAvailableExternalMemorySize();
            free = free.replace("MB", "");
            free = free.replace(",", "");
            int i = Integer.parseInt(free);
            if (i < 1024) {
                i=1024-i;
                Log.e("Gagal","butuh ruang "+i+"mb");
                notice = "Gagal butuh ruang "+i+"mb di sdcard";

            }
            else {
                cekFile();
            }
        }
        public void cekFile() {
            Log.e("Gagal", "cek file");
            File f = new File(Environment.getExternalStorageDirectory()+"/ramp_port.zip");
            if (f.exists()) {
                hapusLama();
            }
            else {
                Log.e("Gagal", "ramp port gk ketemu");
                notice = "Gagal ramp_port.zip tidak ditemukan";
            }
        }
        public void hapusLama(){
            Log.e("Gagal", "hapusLama");
            Process p;
            try {
                p = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(p.getOutputStream());
                os.writeBytes("rm -rf "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/*\n");
                os.writeBytes("rm -rf "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp.zip\n");
                os.writeBytes("exit\n");
                os.flush();
                try {
                    p.waitFor();
                    if (p.exitValue() != 255) {
                        Log.e("Gagal","HapusLama end");

                        unzip();
                    }
                    else {
                    }
                } catch (InterruptedException e) {
                }
            } catch (IOException e) {
            }
        }
        public void unzip(){
            Log.e("Gagal", "unzip");
            Process p;
            try {
                p = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(p.getOutputStream());
                os.writeBytes("unzip /sdcard/ramp_port.zip -d "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp\n");
                os.writeBytes("exit\n");
                os.flush();
                try {
                    p.waitFor();
                    if (p.exitValue() != 255) {
                        Log.e("Gagal","Unzip end");

                        File a = new File(Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/");
                        if (!a.exists()) {
                            Log.e("Gagal", "unzip Error");
                            notice = "Gagal Mungkin tidak ada busybox atau ramp_port.zip bukan rom atau ramp_port.zip rusak";

                        }
                        else {
                            hapusLib();
                        }
                    }
                    else {
                    }
                } catch (InterruptedException e) {
                }
            } catch (IOException e) {
            }


        }
        public void hapusLib(){
            Log.e("Gagal","HapusLib");
            Process p;
            try {
                p = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(p.getOutputStream());
                os.writeBytes("rm -rf "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/check_data_app\n");
                os.writeBytes("rm -rf "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/META-INF\n");
                os.writeBytes("rm -rf "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/logo.bin\n");
                os.writeBytes("rm -rf "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/boot.img\n");
                os.writeBytes("rm -rf "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/build.prop\n");
                os.writeBytes("rm -rf "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/etc/bluetooth\n");
                os.writeBytes("rm -rf "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/etc/firmware\n");
                os.writeBytes("rm -rf "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/etc/vold.fstab\n");
                os.writeBytes("rm -rf "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/etc/vold.fstab.nand\n");
                os.writeBytes("rm -rf "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/lib/hw\n");
                os.writeBytes("rm -rf "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/lib/modules\n");
                os.writeBytes("rm -rf "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/usr\n");
                os.writeBytes("rm -rf "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/vendor\n");
                os.writeBytes("rm -rf "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/xbin/libmnlp_mt6572\n");
                os.writeBytes("exit\n");
                os.flush();
                try {
                    p.waitFor();
                    if (p.exitValue() != 255) {
                        Log.e("Gagal","HapusLib end");

                        File a = new File(Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/etc/bluetooth");
                        File b = new File(Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/etc/firmware");
                        File c = new File(Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/etc/vold.fstab");
                        File d = new File(Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/etc/vold.fstab.nand");
                        File e = new File(Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/lib/hw");
                        File f = new File(Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/etc/lib/modules");
                        File g = new File(Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/etc/usr");
                        File h = new File(Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/etc/vendor");
                        File i = new File(Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/etc/xbin/libmnlp_mt6572");
                                ;

                        if (a.exists() || b.exists() || c.exists() || d.exists() || e.exists() || f.exists() || g.exists() || h.exists() || i.exists() ){
                            Log.e("Gagal","hapusLib ERROR");
                            notice = "Gagal menghapus beberapa file";
                        }
                        else {
                            dumpLib();

                        }
                    }
                } catch (InterruptedException e) {
                }
            } catch (IOException e) {
            }
        }
        public void dumpLib(){
            Log.e("Gagal","dumpLib");
            Process p;
            try {
                p = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(p.getOutputStream());
                os.writeBytes("mount -o rw,remount /system");
                os.writeBytes("cp -R /system/etc/bluetooth "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/etc\n");
                os.writeBytes("cp -R /system/etc/firmware "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/etc\n");
                os.writeBytes("cp -R /system/etc/vold.fstab "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/etc\n");
                os.writeBytes("cp -R /system/etc/vold.fstab.nand "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/etc\n");
                os.writeBytes("cp -R /system/lib/hw "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/lib\n");
                os.writeBytes("cp -R /system/lib/modules "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/lib\n");
                os.writeBytes("cp -R /system/usr "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system\n");
                os.writeBytes("cp -R /system/vendor "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system\n");
                os.writeBytes("cp -R /system/xbin/libmnlp_mt6572 "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/xbin\n");

                os.writeBytes("cp -R /system/etc/bluetooth "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/etc\n");
                os.writeBytes("exit\n");
                os.flush();
                try {
                    p.waitFor();
                    if (p.exitValue() != 255) {
                        Log.e("Gagal","dumpLib end");

                        File a = new File(Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/etc/bluetooth");
                        File b = new File(Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/etc/firmware");
                        File c = new File(Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/etc/vold.fstab");
                        File d = new File(Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/etc/vold.fstab.nand");
                        File e = new File(Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/lib/hw");
                        File f = new File(Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/lib/modules");
                        File g = new File(Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/usr");
                        File h = new File(Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/vendor");
                        File i = new File(Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/xbin/libmnlp_mt6572");
                        ;

                        if (!a.exists()){
                            Log.e("Gagal","dumpLib ERROR a");
                            notice="Gagal dump folder bluetooth ";
                        }
                        else if (!b.exists()){
                            Log.e("Gagal","dumpLib ERROR b");
                            notice="Gagal dump folder firmware ";

                        }
                        else if (!c.exists()) {
                            Log.e("Gagal","dumpLib ERROR c");
                            notice="Gagal dump vold.fstab ";
                        }
                        else if (!d.exists()){
                            Log.e("Gagal","dumpLib ERROR d");
                            notice="Gagal dump vold.fstab.nand ";
                        }
                        else if (!e.exists()){
                            Log.e("Gagal","dumpLib ERROR e");
                            notice="Gagal dump folder hw ";
                        }
                        else if (!f.exists()){
                            Log.e("Gagal","dumpLib ERROR f");
                            notice="Gagal dump folder modules ";

                        }
                        else if (!g.exists()){
                            Log.e("Gagal","dumpLib ERROR g");
                            notice="Gagal dump folder usr ";
                        }
                        else if (!h.exists()){
                            Log.e("Gagal","dumpLib ERROR h");
                            notice="Gagal dump folder vendor ";
                        }
                        else if (!i.exists()){
                            Log.e("Gagal","dumpLib ERROR h");
                            notice="Gagal dump libmnlp_mt6572 ";
                        }

                        else {
                            dropLib();
                        }


                    }

                } catch (InterruptedException e) {
                }
            } catch (IOException e) {
            }
        }
        public void dropLib(){
            Log.e("Gagal","dropLib");
            copyFileOrDir("");
            File a = new File(Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/system/build.prop");
            File b = new File(Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/META-INF");
            if (!a.exists() || !b.exists()){
                Log.e("Gagal","dropLib Gagal");
                notice = "Gagal Mengekstrak asset file";

            }
            else {
                Log.e("Gagal","dropLib end");
                zip();
            }

        }
        public void zip(){
            Log.e("Gagal","zip");
            String a = Environment.getExternalStorageDirectory()+"/OqhaProject/ramp";
            String b = Environment.getExternalStorageDirectory()+"/OqhaProject/ramp.zip";
            try {
                zipFolder(a, b);
            } catch (Exception e) {
                Log.e("Gagal",e+" ngezip" );
            }
            File c = new File(Environment.getExternalStorageDirectory()+"/OqhaProject/ramp.zip");
            if (!c.exists()){
             notice = "Gagal membuat ramp.zip";
            }
            else {
            hapusSampah();
            }
            Log.e("Gagal","zip end");


        }
        public void hapusSampah(){
            Log.e("Gagal", "hapusSampah");
            Process p;
            try {
                p = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(p.getOutputStream());
                os.writeBytes("rm -rf "+Environment.getExternalStorageDirectory()+"/OqhaProject/ramp/*\n");
                os.writeBytes("exit\n");
                os.flush();
                try {
                    p.waitFor();
                    if (p.exitValue() != 255) {
                        Log.e("Gagal","hapusSampah end");
                        notice = "SUKSES!! hasil port terdapat di sdcard/OqhaProject/ramp.zip";

                    }
                    else {
                    }
                } catch (InterruptedException e) {
                }
            } catch (IOException e) {
            }
        }


        public core(Activity activity){

            this.activity = activity;
            this.context = activity;
            this.dialog = new ProgressDialog(activity);
            this.dialog.setTitle("Ramp Chan~");
            this.dialog.setMessage("Working");
            if(!this.dialog.isShowing()){
                this.dialog.show();
            }


        }
        @Override
        protected String doInBackground(Integer... params) {
            root();
            return null;

        }

        @Override
        protected void onPostExecute(String bible){
            this.dialog.dismiss();

            Log.e("Gagal","end "+notice);
            if (notice== null){
            notice = "Gagal";
            }
            end(notice);
        }
    }

}


