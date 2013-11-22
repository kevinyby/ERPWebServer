package com.modules.Util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Random;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * 工具类，生成随机验证码字符串
 *
 */
public class SecurityCode {
    
    /**
     * 验证码难度级别，Simple只包含数字，Medium包含数字和小写英文，Hard包含数字和大小写英文
     */
    public enum SecurityCodeLevel {Simple,Medium,Hard};
    
    /**
     * 产生默认验证码，4位中等难度
     * @return  String 验证码
     */
    public static String getSecurityCode(int count){
        return getSecurityCode(count,SecurityCodeLevel.Hard, true);
    }
    
    /**
     * 产生长度和难度任意的验证码
     * @param length  长度
     * @param level   难度级别
     * @param isCanRepeat  是否能够出现重复的字符，如果为true，则可能出现 5578这样包含两个5,如果为false，则不可能出现这种情况
     * @return  String 验证码
     */
    public static String getSecurityCode(int length,SecurityCodeLevel level,boolean isCanRepeat){
        
        //随机抽取len个字符
        int len=length;
        
        //字符集合(除去易混淆的数字0、数字1、字母l、字母o、字母O)
        char[] codes={'0','1','2','3','4','5','6','7','8','9',
                      'a','b','c','d','e','f','g','h','i',
                      'j','k','m','n','p','q','r','s','t',
                      'u','v','w','x','y','z','A','B','C',
                      'D','E','F','G','H','I','J','K','L',
                      'M','N','P','Q','R','S','T','U','V',
                      'W','X','Y','Z'};
        
        //根据不同的难度截取字符数组
        if(level==SecurityCodeLevel.Simple){
            codes=Arrays.copyOfRange(codes, 0,9);
        }else if(level==SecurityCodeLevel.Medium){
            codes=Arrays.copyOfRange(codes, 0,33);
        }
        
        //字符集合长度
        int n = codes.length;
        
        //抛出运行时异常
        if(len > n && isCanRepeat == false){
            throw new RuntimeException(
                    String.format("调用SecurityCode.getSecurityCode(%1$s,%2$s,%3$s)出现异常，" +
                                   "当isCanRepeat为%3$s时，传入参数%1$s不能大于%4$s",
                                   len,level,isCanRepeat,n));
        }
        
        //存放抽取出来的字符
        char[] result=new char[len];
        
        //判断能否出现重复的字符
        if(isCanRepeat){
            for(int i=0;i<result.length;i++){
                //索引 0 and n-1
                int r=(int)(Math.random()*n);
            
                //将result中的第i个元素设置为codes[r]存放的数值
                result[i]=codes[r];
            }
        }else{
            for(int i=0;i<result.length;i++){
                //索引 0 and n-1
                int r=(int)(Math.random()*n);
                
                //将result中的第i个元素设置为codes[r]存放的数值
                result[i]=codes[r];
                
                //必须确保不会再次抽取到那个字符，因为所有抽取的字符必须不相同。
                //因此，这里用数组中的最后一个字符改写codes[r]，并将n减1
                codes[r]=codes[n-1];
                n--;
            }
        }
        
        return String.valueOf(result);
    }
    
    /**
     * 生成验证码图片
     * @param securityCode   验证码字符
     * @return  BufferedImage  图片
     */
    public static BufferedImage createImage(String securityCode){
        
        //验证码长度
        int codeLength=securityCode.length();
        //字体大小
        int fSize = 20;
        int fWidth = fSize + 1;
        //图片宽度
        int width = codeLength * fWidth + 6 ;
        //图片高度
        int height = fSize * 2 + 1;
        
        //图片
        BufferedImage image=new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.createGraphics();
        Graphics2D g2d = (Graphics2D)g;
        //设置背景色
        g2d.setColor(Color.WHITE);
        //填充背景
        g2d.fillRect(0, 0, width, height);
        
        //设置边框颜色
        g2d.setColor(Color.LIGHT_GRAY);
        //边框字体样式
        g2d.setFont(new Font("Arial", Font.BOLD, height - 2));
        //绘制边框
        g2d.drawRect(0, 0, width - 1, height -1);
        
        
        //绘制噪点
        Random random = new Random();
        //设置噪点颜色
        g2d.setColor(Color.LIGHT_GRAY);
        for(int i = 0;i < codeLength * 6;i++){
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            //绘制1*1大小的矩形
            g2d.drawRect(x, y, 1, 1);
        }
        
        //设置字体颜色和样式
        g2d.setColor(new Color(19,148,246));
        g2d.setFont(new Font("Georgia", Font.BOLD, fSize));
        
      //绘制验证码
        AffineTransform transform = new AffineTransform();
        for(int i = 0; i < codeLength;i++){
        	double theta = Math.PI / (3 + Math.PI);
			double anchorx = i * 16 + 5 ;
			double anchory = height - 10;
			
			transform.setToIdentity();
			transform.rotate(random.nextBoolean() ? theta : theta * -1, anchorx, anchory);
			g2d.setTransform(transform);
            g2d.drawString(String.valueOf(securityCode.charAt(i)), (int)anchorx, (int)anchory);
        }
        //关闭资源
        g2d.dispose();
        
        return image;
    }
    
    /**
     * 返回验证码图片的流格式
     * @param securityCode  验证码
     * @return ByteArrayInputStream 图片流
     */
    public static ByteArrayInputStream getImageAsInputStream(String securityCode) throws Exception {
    	return new ByteArrayInputStream(getImageAsBytes(securityCode));
    }
    
    /**
     * 
     * @param securityCode
     * @return
     * @throws Exception
     */
    public static byte[] getImageAsBytes(String securityCode) throws Exception {
		return getImagesAsOutputStream(securityCode).toByteArray();
	}
    
    /**
     * 
     * @param securityCode
     * @return
     * @throws Exception
     */
    public static ByteArrayOutputStream getImagesAsOutputStream(String securityCode) throws Exception {
    	BufferedImage image = createImage(securityCode);
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JPEGImageEncoder jpeg = JPEGCodec.createJPEGEncoder(bos);
        jpeg.encode(image);
        return bos;
	}
    
    

}
