package com.dyw.quene.controller;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException {
        File dir = new File("E:\\picture");
        /*
         * 列出目录中的图片，得到数组
         * */
        File[] files = dir.listFiles();
        /*
         * 遍历数组
         * */
        for (int x = 0; x < files.length; x++) {
            /*
             * 定义一个数组，存放RGB值
             * */
            int[] rgb = new int[3];
            /*
             * byte转换BuffereImage
             * */
            BufferedImage bImage = null;
            bImage = ImageIO.read(files[x]);

            int width = bImage.getWidth();
            int height = bImage.getHeight();
            int minx = bImage.getMinTileX();
            int miny = bImage.getMinTileY();
            System.out.println("正在处理..." + files[x].getName());

            /*
             * 遍历像素点，判断是否更换颜色
             * */
            for (int i = minx; i < width; i++) {
                for (int j = miny; j < height; j++) {
                    /*
                     * 换色
                     * */
                    int pixel = bImage.getRGB(i, j);
                    rgb[0] = (pixel & 0xff0000) >> 16;
                    rgb[1] = (pixel & 0xff00) >> 8;
                    rgb[2] = (pixel & 0xff);

                    if (rgb[0] < 230 && rgb[0] > 100 && rgb[1] < 230 && rgb[1] > 100 && rgb[2] < 230 && rgb[2] > 100) {
                        bImage.setRGB(i, j, 0xffffff);
                    }
                }
            }
            System.out.println("\t处理完毕：" + files[x].getName());
            System.out.println();
            /*
             * 输出
             * */
            FileOutputStream ops;
            try {
                ops = new FileOutputStream(new File("E:\\picturedone\\" + x + ".jpg"));
                ImageIO.write(bImage, "jpg", ops);
                ops.flush();
                ops.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
}
