package com.example.myapplication2;

import com.example.myapplication2.FileDatasExtracter;

import java.io.File;
import java.util.List;

public class StartMain {
    public static void main(String[] args) {
        // 源文件对象
        File sourceFile = new File("C:\\Users\\LiChao\\Desktop\\China_data,csv");
        // 待搜索地区名称数组
        String[] keys = {"兰州"};

        List<List<String>> listResult = FileDatasExtracter.extractDataListFromFile(sourceFile,keys);

        for (List<String> oneLineData:listResult) {
            System.out.println(oneLineData);
        }

//        System.out.println("\n\n");
//
//        String[][] arrayList = FileDatasExtracter.extractDataArrayFromFile(sourceFile,keys);
//
//        for (int i = 0; i < arrayList.length; i++) {
//            for (int j = 0; j < arrayList[i].length; j++) {
//                System.out.print(arrayList[i][j]);
//                System.out.print(",");
//            }
//            System.out.println();
//        }
    }
}
