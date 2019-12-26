package sample;

import dao.Dao;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import test.fangzhou;


import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private Button button;

    @FXML
    private TextField nowlizhi;
    @FXML
    private TextField meilunlizhi;
    @FXML
    private TextField meilunshijian;
    @FXML
    private CheckBox checkBox;
    @FXML
    private CheckBox yijingshuaguo;
    @FXML
    private TextField meilunyuanshi;


    private int lun_LIZHI = 130;//当前等级理智上限
   // private int bao_LIZHI = 60;//每天的理智包
   // private int suishi=0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        fangzhou f = new fangzhou();

        button.setOnMouseClicked(event -> {
            if (!nowlizhi.getText().equals("") && !meilunlizhi.getText().equals("") && !meilunshijian.getText().equals("") && !meilunyuanshi.getText().equals("")) {
                int now = Integer.parseInt(nowlizhi.getText());
                int meilun = Integer.parseInt(meilunlizhi.getText());
                int time = Integer.parseInt(meilunshijian.getText());
                int lunyuanshi=Integer.parseInt(meilunyuanshi.getText());
                int lizhi = lunyuanshi * lun_LIZHI  + now;
                if (yijingshuaguo.isSelected()){
                    lizhi=now;
                }

                int lun = lizhi / meilun;
                int xiuzhenglun = lun + time * lun / (60 * 6);
                // System.out.println(xiuzhenglun);
                button.setDisable(true);


                f.init();
                Dao dao = new Dao();
                try {

                    for (int i = 0; i < lun; i++) {
                        f.xunhuan(time);
                        System.out.println("进行到第"+i);

                    }
                    dao.beginTransaction();
                    dao.update("insert into context(time,thistext) values('" + simpleDateFormat.format(new Date()) + "','run   " + xiuzhenglun + " times')");

                    dao.commitTransaction();


                } catch (SQLException e) {
                    e.printStackTrace();
                }

                dao.close();
                if (checkBox.isSelected()) {
                    f.shutdown();
                }


            }
        });

    }
}
