package com.ggl.filebrowser.view;

import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class TextAreaFIFO extends JTextArea implements DocumentListener {
	
	private static final long serialVersionUID = 5866722558229907455L;
	private int maxLines;

    public TextAreaFIFO(int lines) {
    	super(10,120);
        maxLines = lines;
        getDocument().addDocumentListener(this);
    }

    public void insertUpdate(DocumentEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                removeLines();
            }
        });
    }

    public void removeUpdate(DocumentEvent e) {
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void removeLines() {
        Element root = getDocument().getDefaultRootElement();
        while (root.getElementCount() > maxLines) {
            Element firstLine = root.getElement(0);
            try {
                getDocument().remove(0, firstLine.getEndOffset());
            } catch (BadLocationException ble) {
                System.out.println(ble);
            }
        }
    }

//    public static void main(String[] args) {
//        final TextAreaFIFO textArea = new TextAreaFIFO(25);
//        textArea.setRows(30);
//        textArea.setColumns(40);
//        JScrollPane scrollPane = new JScrollPane(textArea);
//
//        final Timer timer = new Timer(200, new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                textArea.append(new Date().toString() + "\n");
//            }
//        });
//
//        JButton start = new JButton("Start");
//        start.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                timer.start();
//            }
//        });
//
//        JButton stop = new JButton("Stop");
//        stop.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                timer.stop();
//            }
//        });
//
//        JFrame frame = new JFrame();
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.getContentPane().add(start, BorderLayout.NORTH);
//        frame.getContentPane().add(scrollPane);
//        frame.getContentPane().add(stop, BorderLayout.SOUTH);
//        frame.pack();
//        frame.setVisible(true);
//    }
}