package com.example.myapplication2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class epidemicActivity extends AppCompatActivity {
    String PATHOF;
    String shen=null;
    String shi=null;
    String qu="区级";
    String area=null;
    TextView textView;
    TextView quezhen;
    TextView siwang;
    TextView zhiyu;
    TextView DataTime;
    String textView1;
    String quezhen1;
    String siwang1;
    String zhiyu1;
    String DataTime1;
    String fengxiandiqu;
    Button button;
    Button button1;
    Button button2;
    Button button3;
    String TAG="epidemicActivity";
    private String[] shenArray = {"安徽","北京","重庆","福建","甘肃","广东","广西"
            ,"贵州","海南","河北","河南","黑龙江","湖北","湖南","吉林","江苏","江西"
            ,"辽宁","内蒙古","宁夏","青海","山东","山西","陕西","上海","四川","天津","西藏"
            ,"新疆","云南","浙江"};
    private String[] shiArray=null;
    private String[][] shiArray1={  {"合肥市","芜湖市","蚌埠市","淮南市","马鞍山市","淮北市","铜陵市","安庆市","黄山市","阜阳市","宿州市","滁州市","六安市","宣城市","池州市","亳州市"},
                                    {"东城区","西城区","朝阳区","丰台区","石景山区","海淀区","顺义区","通州区","大兴区","房山区","门头沟区","昌平区","平谷区","密云区","怀柔区","延庆区"},
                                    {"重庆市"},
                                    {"福州市","厦门市","泉州市","漳州市","莆田市","龙岩市","三明市","南平市","宁德市"},
                                    {"兰州市","嘉峪关市","金昌市","白银市","天水市","武威市","张掖市","酒泉市","平凉市","庆阳市","定西市","陇南市"},
                                    {"深圳市","广州市","珠海市","东莞市","佛山市","中山市","惠州市","汕头市","江门市","湛江市","肇庆市","梅州市","茂名市","阳江市","清远市","韶关市","揭阳市","汕尾市","潮州市","河源市","云浮市"},
                                    {"南宁市","柳州市","桂林市","梧州市","北海市","防城港市","钦州市","贵港市","玉林市","百色市","贺州市","河池市","来宾市","崇左市"},
                                    {"贵阳市","遵义市","六盘水市","安顺市","毕节市","铜仁市"},
                                    {"海口市","三亚市","三沙市","儋州市"},
                                    {"石家庄市","唐山市","秦皇岛市","邯郸市","邢台市","保定市","张家口市","承德市","沧州市","廊坊市","衡水市"},
                                    {"郑州市","洛阳市","开封市","漯河市","安阳市","信阳市","南阳市","濮阳市","周口市","新乡市","三门峡市","驻马店市","平顶山市","鹤壁市","商丘市","焦作市","许昌市"},
                                    {"哈尔滨市","齐齐哈尔市","鹤岗市","双鸭山市","鸡西市","大庆市","伊春市","牡丹江市","佳木斯市","七台河市","黑河市","绥化市"},
                                    {"武汉市","黄石市","襄阳市","荆州市","宜昌市","十堰市","孝感市","荆门市","鄂州市","黄冈市","咸宁市","随州市"},
                                    {"长沙市","株洲市","湘潭市","衡阳市","邵阳市","岳阳市","常德市","张家界市","益阳市","郴州市","永州市","怀化市","娄底市"},
                                    {"长春市","吉林市","四平市","辽源市","通化市","白山市","松原市","白城市"},
                                    {"苏州市","无锡市","常州市","镇江市","南京市","南通市","扬州市","泰州市","盐城市","淮安市","宿迁市","徐州市","连云港市"},
                                    {"南昌市","九江市","萍乡市","鹰潭市","上饶市","抚州市","新余市","宜春市","景德镇市","吉安市","赣州市"},
                                    {"沈阳市","大连市","鞍山市","抚顺市","本溪市","丹东市","锦州市","营口市","阜新市","辽阳市","盘锦市","铁岭市","朝阳市","葫芦岛市"},
                                    {"呼和浩特市","包头市","鄂尔多斯市","乌海市","赤峰市","通辽市","呼伦贝尔市","乌兰察布市","巴彦淖尔市"},
                                    {"银川市","石嘴山市","吴忠市","固原市","中卫市"},
                                    {"西宁市","海东市"},
                                    {"济南市","青岛市","淄博市","枣庄市","东营市","烟台市","潍坊市","济宁市","泰安市","威海市","日照市","临沂市","德州市","聊城市","滨州市","菏泽市"},
                                    {"太原市","大同市","晋城市","运城市","晋中市","吕梁市","临汾市","阳泉市","长治市","忻州市","朔州市"},
                                    {"西安市","咸阳市","铜川市","渭南市","延安市","榆林市","汉中市","安康市","商洛市","宝鸡市"},
                                    {"上海市"},
                                    {"成都市","绵阳市","自贡市","攀枝花市","泸州市","德阳市","广元市","遂宁市","内江市","乐山市","资阳市","宜宾市","南充市","达州市","雅安市","广安市","巴中市","眉山市"},
                                    {"天津市"},
                                    {"拉萨市"},
                                    {"乌鲁木齐市","克拉玛依市","吐鲁番市","哈密市"},
                                    {"昆明市","曲靖市","昭通市","玉溪市","楚雄州市","红河州市","文山州市","普洱市","版纳州市","大理州市","保山市","德宏州市","丽江市","怒江州市","迪庆州市","临沧市"},
                                    {"杭州市","宁波市","温州市","绍兴市","湖州市","嘉兴市","金华市","衢州市","台州市","丽水市","舟山市"},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epidemic);
        File dir=new File(getExternalFilesDir(null),"sounds");
        if(!dir.exists()){
            dir.mkdirs();
        }
        PATHOF=dir.getPath();
        textView = (TextView) findViewById(R.id.diqu);
        quezhen=(TextView)findViewById(R.id.quezhen);
        siwang=(TextView)findViewById(R.id.siwang);
        zhiyu=(TextView)findViewById(R.id.zhiyu);
        DataTime=(TextView)findViewById(R.id.DataTime);
        sheninitSpinner();

//        area=shen+" "+shi+" "+qu;
        area = MainActivity.KeyLocationInfo[0].substring(0, MainActivity.KeyLocationInfo[0].length() - 1) + " " + MainActivity.KeyLocationInfo[2] + " " + MainActivity.KeyLocationInfo[2];
        button=(Button) findViewById(R.id.yiqing);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                StartMain(PATHOF,shi.substring(0, shi.length()-1));
//                System.out.println(shi.substring(0, shi.length()-1));
                StartMain(PATHOF,MainActivity.KeyLocationInfo[1].substring(0, MainActivity.KeyLocationInfo[1].length() - 1));
                quezhen.setText("新增确诊："+quezhen1);
                siwang.setText("新增死亡："+siwang1);
                zhiyu.setText("治愈："+zhiyu1);
                DataTime.setText("数据更新时间："+DataTime1);
            }
        });
        button1=(Button) findViewById(R.id.update);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //从服务器处获取更新数据
                Download download=new Download();
                download.start();
                Toast.makeText(epidemicActivity.this, "更新完成", Toast.LENGTH_SHORT).show();
            }
        });
        button2=(Button) findViewById(R.id.school);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(epidemicActivity.this,SchoolActivity.class);
                startActivity(intent);
            }
        });

        button3=(Button) findViewById(R.id.realschool);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(epidemicActivity.this,SchoolActivity2.class);
                startActivity(intent);
            }
        });

    }

    private void sheninitSpinner() {
        ArrayAdapter<String> starAdapter = new ArrayAdapter<String>(this,R.layout.item_select,shenArray);
        //设置数组适配器的布局样式
        starAdapter.setDropDownViewResource(R.layout.item_dropdown);
        //从布局文件中获取名叫sp_dialog的下拉框
        Spinner sp = findViewById(R.id.shen);
        //设置下拉框的标题，不设置就没有难看的标题了
        sp.setPrompt("请选择省份");
        //设置下拉框的数组适配器
        sp.setAdapter(starAdapter);
        //设置下拉框默认的显示第一项
        sp.setSelection(0);
        //给下拉框设置选择监听器，一旦用户选中某一项，就触发监听器的onItemSelected方法
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                shen=shenArray[i];
//                area=shen+" "+shi+" "+qu;
                area = MainActivity.KeyLocationInfo[0].substring(0, MainActivity.KeyLocationInfo[0].length() - 1) + " " + MainActivity.KeyLocationInfo[2] + " " + MainActivity.KeyLocationInfo[2];
                textView.setText("地区："+area);
                shiArray=shiArray1[i];
                shiinitSpinner();
                System.out.println();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void shiinitSpinner() {
        ArrayAdapter<String> starAdapter = new ArrayAdapter<String>(this,R.layout.item_select,shiArray);
        //设置数组适配器的布局样式
        starAdapter.setDropDownViewResource(R.layout.item_dropdown);
        //从布局文件中获取名叫sp_dialog的下拉框
        Spinner sp = findViewById(R.id.shi);
        //设置下拉框的标题，不设置就没有难看的标题了
        sp.setPrompt("请选择省份");
        //设置下拉框的数组适配器
        sp.setAdapter(starAdapter);
        //设置下拉框默认的显示第一项
        sp.setSelection(0);
        //给下拉框设置选择监听器，一旦用户选中某一项，就触发监听器的onItemSelected方法
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                shi=shiArray[i];
//                area=shen+" "+shi+" "+qu;
                area = MainActivity.KeyLocationInfo[0].substring(0, MainActivity.KeyLocationInfo[0].length() - 1) + " " + MainActivity.KeyLocationInfo[2] + " " + MainActivity.KeyLocationInfo[2];
                //quinitSpinner();
                textView.setText("地区："+area);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


        private  void StartMain(String path,String key) {
            // 源文件对象
            File sourceFile = new File(path+"/China_data.csv");
            // 待搜索地区名称数组
            String[] keys = {key};

            List<List<String>> listResult = FileDatasExtracter.extractDataListFromFile(sourceFile,keys);

            for (List<String> oneLineData:listResult) {
                System.out.println(oneLineData);
                String[] strs = oneLineData.toArray(new String[]{});
                quezhen1=strs[1];
                siwang1=strs[2];
                zhiyu1=strs[3];
                DataTime1=strs[4];
                System.out.println(strs[0]);
            }

        }

        public class Download extends Thread {
            @Override
            public void run() {
//从服务器处获取更新数据
                int readlen;
                boolean success;
                String targetPath=PATHOF+"/China_data.csv";
                try {
                    URL url = new URL("http://202.182.112.88:23333/down/7Mp2VPGvbi3V");
                    URLConnection connection=url.openConnection();
                    connection.connect();
                    InputStream is = url.openStream();
                    FileOutputStream fos = new FileOutputStream(targetPath);
                    byte[] bytes = new byte[1024];
                    while ((readlen=is.read(bytes))!=-1){
                        fos.write(bytes, 0, readlen);
                    }
                    success=true;
                    is.close();
                    fos.close();
                    System.out.println("下载完成");
                    Log.e(TAG,"下载完成");

                } catch (IOException e) {
                    e.printStackTrace();

                    success=false;
                }

            }

        }


//    private void quinitSpinner() {
//        ArrayAdapter<String> starAdapter = new ArrayAdapter<String>(this,R.layout.item_select,shenArray);
//        //设置数组适配器的布局样式
//        starAdapter.setDropDownViewResource(R.layout.item_dropdown);
//        //从布局文件中获取名叫sp_dialog的下拉框
//        Spinner sp = findViewById(R.id.qu);
//        //设置下拉框的标题，不设置就没有难看的标题了
//        sp.setPrompt("请选择省份");
//        //设置下拉框的数组适配器
//        sp.setAdapter(starAdapter);
//        //设置下拉框默认的显示第一项
//        sp.setSelection(0);
//        //给下拉框设置选择监听器，一旦用户选中某一项，就触发监听器的onItemSelected方法
//        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                qu=shenArray[i];
//                area=shen+" "+shi+" "+qu;
//                textView.setText("地区："+area);
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });
//    }

}