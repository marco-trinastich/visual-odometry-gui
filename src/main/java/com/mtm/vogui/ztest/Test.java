/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.ztest;

/*
import boofcv.io.video.CombineFilesTogether;
import boofcv.io.video.CreateMJpeg;
import boofcv.misc.BoofMiscOps;
import boofcv.struct.image.ImageFloat32;
*/
import boofcv.struct.image.ImageGray;
import boofcv.struct.image.GrayU8;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.*;

@SuppressWarnings("rawtypes")
public class Test<T extends ImageGray<T>> {

    Class<T> imgType;

    /**
     * @param args
     */
    public static void main(String[] args) {
        // test();
        // buffertest();
        // test2();
    }

    public static void test2() {
        // String a = "Fotocamera HD FaceTime (integrata) 0x8020000005ac8514";
        // String b = "Fotocamera di iPhone di Marco
        // ADFEB003-49E9-462C-ABA2-89A200000001";
    }

    public static void buffertest() {
        final ArrayList<String> buffer = new ArrayList<>();
        final int buff_size = 10;

        final ArrayList<String> sourcedata = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            sourcedata.add(String.valueOf(i));
        }

        Thread producer = new Thread(new Runnable() {

            int data_pos = -1;

            @Override
            public void run() {

                while (true) {

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (data_pos == 99)
                        return;

                    if (buffer.size() < buff_size) {
                        data_pos++;
                        buffer.add(sourcedata.get(data_pos));
                    } else {
                        System.out.println("BUFFER FULL!");
                        data_pos++; // Flusso input continuo (i dati vengono prodotti continuamente anche se il
                                    // buffer è pieno)
                        // ==> Se PRODUCER è più veloce di CONSUMER, perdiamo dati
                    }

                }

            }

        });

        Thread consumer = new Thread(new Runnable() {

            @Override
            public void run() {

                while (true) {

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (buffer.size() > 0) {
                        String read_data = buffer.get(0);
                        buffer.remove(0);
                        System.out.println(read_data);
                    } else {
                        System.out.println("BUFFER EMPTY!");
                    }

                }
            }

        });

        producer.start();
        consumer.start();

    }

    public static void test() {
        String a = " 1 , 20 , 30,   40   , 168";

        int[] nums = extract_IntArray(a);

        if (nums != null) {
            for (int i = 0; i < nums.length; i++) {
                System.out.println(nums[i]);
            }
        } else {
            System.out.println("NULL");
        }

        /* 
        TrackerFactory c = null;
        try {
            c = TrackerFactory.from(ImageTypeDescriptor.GrayU8);
            PointTracker<GrayU8> p = (PointTracker<GrayU8>) c.createDefault().instance();
        } catch (InvalidImageFormatException ignored) {
        }
        */

        double b = 1.111;
        System.out.println(b);
        System.out.println(Math.round(b));
        System.out.println(Math.round(9 * b));
        System.out.println((int) (b));
        System.out.println((int) (9 * b));

        System.out.println();
        for (int i = 1; i < 11; i++) {
            System.out.println(i % 10);
        }

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                @SuppressWarnings("unused")
                ScrollPanePaint a;
                a = new ScrollPanePaint();
            }
        });

        JPanel test_panel = new JPanel() {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void paint(Graphics g) {
                g.fillOval(25, 25, 120, 120);
                g.setColor(Color.red);
                g.drawLine(0, 0, 85, 85);
            }

        };

        JFrame test_frame = new JFrame();
        test_frame.getContentPane().add(test_panel);

        test_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        test_frame.setSize(200, 200);
        test_frame.setVisible(true);

        Test app = new Test();
        app.classEqualsClass();
    }

    @SuppressWarnings("unchecked")
    public void classEqualsClass() {
        imgType = (Class<T>) GrayU8.class;
        System.out.println(imgType.equals(GrayU8.class));
    }

    /**
     * @see <a href="http://stackoverflow.com/a/10097538/230513">Link1</a>
     * @see <a href="http://stackoverflow.com/a/2846497/230513">Link2</a>
     * @see <a href="http://stackoverflow.com/a/3518047/230513">Link3</a>
     */
    public static class ScrollPanePaint extends JFrame {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private static final int TILE = 64;

        public ScrollPanePaint() {
            JViewport viewport = new MyViewport();
            viewport.setView(new MyPanel());
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setViewport(viewport);
            this.add(scrollPane);
            this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            this.pack();
            this.setLocationRelativeTo(null);
            this.setVisible(true);
        }

        private class MyViewport extends JViewport {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public MyViewport() {
                this.setOpaque(false);
                this.setPreferredSize(new Dimension(6 * TILE, 6 * TILE));
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                g.setColor(Color.blue);
                g.fillRect(TILE, TILE, 3 * TILE, 3 * TILE);
            }
        }

        private class MyPanel extends JPanel {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public MyPanel() {
                this.setOpaque(false);
                this.setPreferredSize(new Dimension(9 * TILE, 9 * TILE));
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.lightGray);
                int w = this.getWidth() / TILE + 1;
                int h = this.getHeight() / TILE + 1;
                for (int row = 0; row < h; row++) {
                    for (int col = 0; col < w; col++) {
                        if ((row + col) % 2 == 0) {
                            g.fillRect(col * TILE, row * TILE, TILE, TILE);
                        }
                    }
                }
            }
        }

    }

    // extracts an array of integer from strings formatted this way: 1,2,3,4 (comma
    // separated numbers)
    static private int[] extract_IntArray(String string) {

        int[] result = null;

        String stringpart = string;

        while (!stringpart.equalsIgnoreCase("")) {
            int firstcomma = stringpart.indexOf(",");
            if (firstcomma != -1) {
                try {
                    int value = Integer.parseInt(stringpart.substring(0, firstcomma).trim());
                    if (result == null) {
                        result = new int[1];
                        result[0] = value;
                    } else {
                        int[] tmp = result;
                        result = new int[tmp.length + 1];
                        System.arraycopy(tmp, 0, result, 0, tmp.length);
                        result[result.length - 1] = value;
                    }
                    if (stringpart.length() == firstcomma + 1)
                        return null;
                    stringpart = stringpart.substring(firstcomma + 1, stringpart.length());
                } catch (Exception e) {
                    return null;
                }
            } else {
                try {
                    int value = Integer.parseInt(stringpart.trim());
                    if (result == null) {
                        result = new int[1];
                        result[0] = value;
                    } else {
                        int[] tmp = result;
                        result = new int[tmp.length + 1];
                        System.arraycopy(tmp, 0, result, 0, tmp.length);
                        result[result.length - 1] = value;
                    }
                    stringpart = "";
                } catch (Exception e) {
                    return null;
                }
            }
        }

        return result;
    }
}
