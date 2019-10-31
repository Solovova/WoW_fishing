package sample;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;

public class LogoController {

    public Slider sldFindBlue;
    public Label labBlue;

    public Slider sldFindRed;
    public Label labRed;

    public ImageView imgBig;
    public AnchorPane anchorImage;
    public ImageView imgMonFirst;
    public ImageView imgMon;
    public Label labDiff;
    public Label labDark;
    public Slider sldFindDark;
    public TextField tfCorrectionR;
    public TextField tfCorrectionG;
    public TextField tfCorrectionB;
    public CheckBox cbRun;
    public TextField tfCounter;

    private WritableImage imageFirst;
    private WritableImage imageMon;

    private WritableImage writableImage;
    private WritableImage writableImageOrig;


    public LogoController() {
    }

    @FXML
    public void initialize() {
        sldFindBlue.valueProperty().addListener(e -> {
                    labBlue.setText("Blue: " + Integer.toString((int) sldFindBlue.getValue()));
                }
        );

        sldFindRed.valueProperty().addListener(e -> {
                    labRed.setText("Red: " + Integer.toString((int) sldFindRed.getValue()));
                }
        );

        sldFindDark.valueProperty().addListener(e -> {
                    labDark.setText("Dark: " + Integer.toString((int) sldFindDark.getValue()));
                }
        );
    }

    private void fillBigImage() {
        writableImage = copyImage(writableImageOrig);
        PixelReader pixelReader = writableImage.getPixelReader();
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        double corRSum = 0;
        double corGSum = 0;
        double corBSum = 0;

        for (int x = 0; x < writableImage.getWidth(); x++) {
            for (int y = 0; y < writableImage.getHeight(); y++) {
                Color pix = pixelReader.getColor(x, y);
                corRSum += pix.getRed();
                corGSum += pix.getGreen();
                corBSum += pix.getBlue();
            }
        }

        tfCorrectionR.setText(Integer.toString((int) (corRSum / writableImage.getWidth() / writableImage.getHeight() * -255)));
        tfCorrectionG.setText(Integer.toString((int) (corGSum / writableImage.getWidth() / writableImage.getHeight() * -255)));
        tfCorrectionB.setText(Integer.toString((int) (corBSum / writableImage.getWidth() / writableImage.getHeight() * -255)));

        for (int x = 0; x < writableImage.getWidth(); x++) {
            for (int y = 0; y < writableImage.getHeight(); y++) {
                Color pix = pixelReader.getColor(x, y);
                int corR = Integer.parseInt(tfCorrectionR.getText());
                int corG = Integer.parseInt(tfCorrectionG.getText());
                int corB = Integer.parseInt(tfCorrectionB.getText());
                if (corR != 0 || corG != 0 || corB != 0) {
                    corR += (int) (pix.getRed() * 255);
                    if (corR > 255) corR = 255;
                    if (corR < 0) corR = 0;
                    corG += (int) (pix.getGreen() * 255);
                    if (corG > 255) corG = 255;
                    if (corG < 0) corG = 0;
                    corB += (int) (pix.getBlue() * 255);
                    if (corB > 255) corB = 255;
                    if (corB < 0) corB = 0;

                    Color correctedPix = Color.rgb(corR, corG, corB);
                    pixelWriter.setColor(x, y, correctedPix);
                    pix = correctedPix;
                }

                int dark = (int) ((pix.getBlue() + pix.getGreen() + pix.getRed()) / 3 * 255);
                if (dark > sldFindDark.getValue()) continue;

                if ((pix.getBlue() - pix.getGreen() > sldFindBlue.getValue() / 255) || (pix.getBlue() - pix.getRed()) > sldFindBlue.getValue() / 255)
                    pixelWriter.setColor(x, y, Color.rgb(0, 0, 255));

                if ((pix.getRed() - pix.getGreen() > sldFindRed.getValue() / 255) || (pix.getRed() - pix.getBlue()) > sldFindRed.getValue() / 255)
                    pixelWriter.setColor(x, y, Color.rgb(255, 0, 0));
            }
        }
        setImgBig(writableImage);

    }

    private WritableImage copyImage(WritableImage image) {
        int height = (int) image.getHeight();
        int width = (int) image.getWidth();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                pixelWriter.setColor(x, y, color);
            }
        }
        return writableImage;
    }

    private void snapshot() throws AWTException {
        Robot robot = new Robot();
        BufferedImage bi = robot.createScreenCapture(new Rectangle(0, 0, 1920, 1080));
        writableImageOrig = SwingFXUtils.toFXImage(bi, null);
        writableImage = copyImage(writableImageOrig);
        setImgBig(writableImage);
    }

    public void btnCapture(ActionEvent actionEvent) throws AWTException {
        snapshot();
    }

    private void setImgBig(WritableImage wi) {
        int sizeX = (int) wi.getWidth();
        int sizeY = (int) wi.getHeight();
        imgBig.setFitHeight(sizeY);
        imgBig.setFitWidth(sizeX);

        anchorImage.setPrefHeight(sizeY);
        anchorImage.setPrefWidth(sizeX);
        imgBig.setImage(wi);
    }

    public void btnFind(ActionEvent actionEvent) {
        fillBigImage();
    }

    public void btnSave(ActionEvent actionEvent) throws IOException {
        BufferedImage bImage = SwingFXUtils.fromFXImage(writableImageOrig, null);
        ImageIO.write(bImage, "PNG", new File("C://Users/solo/Desktop/img.png"));
    }

    public void btnLoad(ActionEvent actionEvent) throws IOException {
        BufferedImage bImage = ImageIO.read(new File("C://Users/solo/Desktop/img.png"));
        writableImageOrig = SwingFXUtils.toFXImage(bImage, null);
        writableImage = copyImage(writableImageOrig);
        setImgBig(writableImage);
    }

    private int[] findObject(int minX, int maxX, int minY, int maxY) {
        int countRed = 0;
        int countBlue = 0;

        PixelReader pixelReader = writableImage.getPixelReader();


        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                Color color = pixelReader.getColor(x, y);
                if (color.equals(Color.rgb(255, 0, 0))) countRed++;
                if (color.equals(Color.rgb(0, 0, 255))) countBlue++;
            }
        }

        return new int[]{countBlue, countRed};
    }

    private int[] findPole() {
        int maxBlue = 0;
        int maxRed = 0;
        int maxX = 0;
        int maxY = 0;

        int step = 5;
        int bar = 40;
        int width = (int) (writableImage.getWidth() / 4 * 3);
        int height = (int) (writableImage.getHeight() / 5 * 3);
        for (int sx = 0; sx < (width - bar) / step; sx++) {
            for (int sy = 0; sy < (height - bar) / step; sy++) {
                int[] result = findObject(sx * step, sx * step + bar, sy * step, sy * step + bar);
                if (result[0] > maxBlue && result[1] > maxRed) {
                    maxBlue = result[0];
                    maxRed = result[1];
                    maxX = sx * step;
                    maxY = sy * step;
                }
            }
        }
        return new int[]{maxX + bar, maxY + bar};
    }

    public void btnObject(ActionEvent actionEvent) {
        int[] result = findPole();
        int maxX = result[0];
        int maxY = result[1];

        if (maxX != 0 && maxY != 0) {

            PixelWriter pixelWriter = writableImage.getPixelWriter();
            for (int x = maxX; x < maxX + 40; x++) {
                for (int y = maxY; y < maxY + 40; y++) {
                    pixelWriter.setColor(x, y, Color.rgb(0, 0, 0));
                }
            }
        }
    }

    private void doStartSnapShot(int x, int y) throws AWTException {
        int width = 100;
        int height = 100;

        Robot robot = new Robot();
        BufferedImage bi = robot.createScreenCapture(new Rectangle(x - width / 2, y - width / 2, width, height));
        imageFirst = SwingFXUtils.toFXImage(bi, null);
        imgMonFirst.setImage(imageFirst);
    }

    private void doMonSnapShot(int x, int y) throws AWTException {
        int width = 100;
        int height = 100;

        Robot robot = new Robot();
        BufferedImage bi = robot.createScreenCapture(new Rectangle(x - width / 2, y - width / 2, width, height));
        imageMon = SwingFXUtils.toFXImage(bi, null);
        imgMon.setImage(imageMon);
    }

    private double getDiff() {
        int width = (int) imageFirst.getWidth();
        int height = (int) imageFirst.getHeight();

        PixelReader pixelReaderFirst = imageFirst.getPixelReader();
        PixelReader pixelReaderMon = imageMon.getPixelReader();
        double diff = 0.0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color c1 = pixelReaderFirst.getColor(x, y);
                Color c2 = pixelReaderMon.getColor(x, y);
                diff = diff + c1.getGreen() - c2.getGreen() + c1.getBlue() - c2.getBlue() + c1.getRed() - c2.getRed();
            }
        }

        return diff;
    }


    public void btnStartMonitor(ActionEvent actionEvent) throws AWTException {
        cbRun.setSelected(!cbRun.isSelected());
        if (cbRun.isSelected()) fishingThread();
    }

    private void fishingThread() {
        Runnable myRunnable =
                new Runnable() {
                    public void run() {
                        while (cbRun.isSelected()) {
                            try {
                                Robot robot = new Robot();
                                robot.mouseMove(20, 20);
                                Thread.sleep(200);
                                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                                Thread.sleep(200);
                                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                                Thread.sleep(200);
                                robot.keyPress(KeyEvent.VK_9);
                                Thread.sleep(200);
                                robot.keyRelease(KeyEvent.VK_9);
                                Thread.sleep(2000);

                                snapshot();
                                fillBigImage();
                                int[] result = findPole();
                                int maxX = result[0];
                                int maxY = result[1];

                                if (maxX != 0 && maxY != 0) {

                                    robot.mouseMove(maxX, maxY);
                                    Thread.sleep(500);

                                    doStartSnapShot(maxX, maxY);

                                    for (int t = 0; t < 35; t++) {
                                        Thread.sleep(500);
                                        doMonSnapShot(maxX, maxY);
                                        Thread.sleep(200);
                                        double diff = getDiff();
                                        double diffHook = 800.0 - (800.0 - (double) maxY) / 800.0 * 500.0; //0 - 300, 800 - 800

                                        System.out.println(diff);

                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                labDiff.setText(Double.toString(diff) + "/" + Double.toString(diffHook));
                                            }
                                        });

                                        if (diff < -diffHook || diff > diffHook) {
                                            Thread.sleep(200);
                                            robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                                            Thread.sleep(200);
                                            robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                                            Thread.sleep(1000);
                                            break;
                                        }
                                    }
                                }

                                if (!getCounter()) {
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            cbRun.setSelected(false);
                                        }
                                    });
                                }

                            } catch (AWTException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }


                    }
                };

        Thread t = new Thread(myRunnable);
        t.start();
    }

    private boolean getCounter() {
        int counter = Integer.parseInt(tfCounter.getText());
        counter--;

        final int finCounter = counter;

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                tfCounter.setText(Integer.toString(finCounter));
            }
        });

        if (counter / 20 * 20 == counter) {
            try {
                Robot robot = new Robot();
                robot.delay(100);
                robot.keyPress(KeyEvent.VK_F10);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_F10);
                robot.delay(300);

                robot.keyPress(KeyEvent.VK_F11);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_F11);
                robot.delay(300);

                robot.keyPress(KeyEvent.VK_F9);
                robot.delay(100);
                robot.keyRelease(KeyEvent.VK_F9);
                robot.delay(300);

                IntStream.range(0, 5).forEach(
                        n -> {
                            robot.keyPress(KeyEvent.VK_F12);
                            robot.delay(100);
                            robot.keyRelease(KeyEvent.VK_F12);
                            robot.delay(300);
                        });


//                robot.keyPress(KeyEvent.VK_SPACE);
//                robot.delay(100);
//                robot.keyRelease(KeyEvent.VK_SPACE);
//                robot.delay(500);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }

        return counter > 0;
    }
}
