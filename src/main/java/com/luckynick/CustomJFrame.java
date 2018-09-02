package com.luckynick;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public abstract class CustomJFrame extends JFrame implements Runnable {

    Thread thisThread;

    public CustomJFrame(String title) {
        super(title);
        configureWindow();
    }

    protected void configureWindow() {
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                dispose();
            }
        });
    }

    protected abstract void addElements();


    public void displayWindow() {
        // Launch the window
        pack();
        setVisible(true);
        setExtendedState(JFrame.ICONIFIED);
        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        setExtendedState(JFrame.NORMAL);
    }

    protected void hardResetWindow() {
        getContentPane().removeAll();
        addElements();
        displayWindow();
    }

    protected void refreshView() {
        repaint();
        displayWindow();
    }


    @Override
    public void run() {
        synchronized (this) {
            try {
                thisThread = Thread.currentThread();
                wait();
            }
            catch (InterruptedException e) {
                System.out.println(thisThread.getName() + " was stopped.");
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        thisThread.interrupt();
    }
}
