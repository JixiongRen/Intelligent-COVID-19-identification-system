package com.example.myapplication2;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据提取器
 */
public class FileDatasExtracter {
    /**
     * 从文件中提取数据
     * @param sourceFile 存放数据的源文件
     * @param keys 筛选的关键字
     * @return 被筛选提取出的结果列表
     */
    public static List<List<String>> extractDataListFromFile(File sourceFile, String[] keys){
        List<List<String>> result = new ArrayList<>();

        String[] searchKeys = new String[keys.length];

        for(int i=0;i<keys.length;i++){
            searchKeys[i] = keys[i] + ",";
        }

        // 读取文件内容，并为数据行列表赋值
        try {
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            BufferedReader reader = new BufferedReader(inputStreamReader);

            // 切割后的字符串数组
            String[] oneLineData = null;
            // 结果数组
            List<String> oneLineDataList = null;
            // 单行数据是否符合筛选要求
            boolean isRightData = false;

            // 读取第一行，跳过标题行
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                isRightData = false;

                for(String oneKey:searchKeys){
                    if(line.startsWith(oneKey)){
                        isRightData = true;
                        break;
                    }
                }

                if(true == isRightData){
                    oneLineData = line.split(",");
                    oneLineDataList = new ArrayList<>();

                    for (int i=0;i<oneLineData.length;i++){
                        oneLineDataList.add(oneLineData[i].trim());
                    }

                    result.add(oneLineDataList);
                }
            }

            reader.close();
            inputStreamReader.close();
            fileInputStream.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            System.out.println("读取文件为一个内存字符串失败，失败原因是使用了不支持的字符编码UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("读取文件为一个内存字符串失败，失败原因所给的文件【" + sourceFile + "】不存在！");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("读取文件为一个内存字符串失败，失败原因是读取文件异常！");
        }

        return result;
    }

    /**
     * 从文件中提取数据
     * @param sourceFile 存放数据的源文件
     * @param keys 筛选的关键字
     * @return 被筛选提取出的结果数组
     */
    public static String[][] extractDataArrayFromFile(File sourceFile, String[] keys){
        List<List<String>> listResult = extractDataListFromFile(sourceFile,keys);

        String[][] result = new String[listResult.size()][];

        for (int i = 0; i < listResult.size(); i++) {
            String[] oneLineDatas = new String[listResult.get(i).size()];

            for (int j = 0; j < listResult.get(i).size(); j++) {
                oneLineDatas[j] = listResult.get(i).get(j);
            }

            result[i] = oneLineDatas;
        }

        return result;
    }
}