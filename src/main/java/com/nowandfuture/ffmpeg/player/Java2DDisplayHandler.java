package com.nowandfuture.ffmpeg.player;

import com.nowandfuture.ffmpeg.Frame;
import com.nowandfuture.ffmpeg.IMediaPlayer;
import com.nowandfuture.ffmpeg.Java2DFrameConverter;

import javax.swing.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class Java2DDisplayHandler implements PlayHandler {

    JLabel label = new JLabel();
    JFrame jFrame = new JFrame();
    BufferedImage image = null;

    @Override
    public void init(IMediaPlayer.SyncInfo info) {
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.add(label);
        jFrame.setVisible(true);
    }

    @Override
    public void handle(Frame frame) {
        image = new BufferedImage(frame.imageWidth,frame.imageHeight,BufferedImage.TYPE_3BYTE_BGR);
        Java2DFrameConverter.copy(frame,image);

        if(image != null) {
            if(label.getWidth() <= 0 || label.getHeight() <= 0){
                label.setSize(image.getWidth(), image.getHeight()+100);

                jFrame.setSize(label.getWidth(), label.getHeight());
            }


            BufferedImage image2 = scaleWithAffineTransformOp(image,jFrame.getWidth(),jFrame.getHeight(), AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            image = null;
            ImageIcon imageIcon = new ImageIcon(image2);
            label.setIcon(imageIcon);
        }
    }

    @Override
    public void flush() {

    }

    @Override
    public void destroy() {
        jFrame.dispose();
    }

    private static BufferedImage scaleWithAffineTransformOp(
            BufferedImage image, int w, int h,
            int renderingHints)
    {
        BufferedImage scaledImage = new BufferedImage(w, h, image.getType());
        double scaleX = (double) w / image.getWidth();
        double scaleY = (double) h / image.getHeight();
        AffineTransform affineTransform =
                AffineTransform.getScaleInstance(scaleX, scaleY);
        AffineTransformOp affineTransformOp = new AffineTransformOp(
                affineTransform, renderingHints);
        return affineTransformOp.filter(
                image, scaledImage);
    }
}
