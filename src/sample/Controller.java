package sample;

import com.android.apksig.ApkSigner;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;

import java.io.*;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    @FXML
    public TextArea log;
    @FXML
    public TextField out;
    @FXML
    public TextField put;
    public String putPath;


    public void clear() {
        this.out.setText("");
        this.put.setText("");
    }


    public void putDragDropped(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        if (dragboard.hasFiles()) {
            try {
                File file = (File) dragboard.getFiles().get(0);
                if (file != null) {
                    this.putPath = file.getAbsolutePath();
                    this.put.setText(this.putPath);
                    this.out.setText(getOutPath(this.putPath));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getOutPath(String str) {
        return str.replace(".apk", "_sign.apk");
    }


    public void collect() {
        try {
            process();
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    public void process() throws Exception {
        File srcApk = new File(this.put.getText());
        File outApk = new File(this.out.getText());
        String signFile = "test.jks";//同时支持keystore格式,直接更换名字即可
        String signPassword = "123456";//密码
        String signAlias = "123456";//别名
        String signAliasPassword = "123456";//别名密码

        this.log.clear();
        this.log.appendText("\n正在读取APK：" + srcApk.getPath());
        KeyStore keyStore=KeyStore.getInstance("JKS");

        //读取签名文件到inputStream
        File sf = new File(signFile);
        FileInputStream is = new FileInputStream(sf);
        keyStore.load(is,signPassword.toCharArray());
        is.close();

        String alias = keyStore.aliases().nextElement();
        PrivateKey privateKey= (PrivateKey) keyStore.getKey(signAlias,signAliasPassword.toCharArray());
        X509Certificate x509Certificate= (X509Certificate) keyStore.getCertificate(alias);

        //证书列表
        List<X509Certificate> mCertificates = new ArrayList<>();
        mCertificates.add(x509Certificate);
        ApkSigner.SignerConfig signerConfig =
                new ApkSigner.SignerConfig.Builder(
                        "apkSigner", privateKey, mCertificates)
                        .build();
        //signerConfig
        List<ApkSigner.SignerConfig> signerConfigs = new ArrayList<>();
        signerConfigs.add(signerConfig);
        ApkSigner ApkSigner=new ApkSigner.Builder(signerConfigs)
                .setCreatedBy("apkSigner")
                .setInputApk(srcApk)
                .setOutputApk(outApk)
                .setMinSdkVersion(15)
                .setV1SigningEnabled(true)
                .setV2SigningEnabled(true)
                .setV3SigningEnabled(true)
                .setDebuggableApkPermitted(true)
                .build();
        ApkSigner.sign();
        this.log.appendText("\n完成签名：" + srcApk.getPath());
    }

}
