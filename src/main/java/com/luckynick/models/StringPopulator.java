package com.luckynick.models;

import com.luckynick.CustomJFrame;
import com.luckynick.shared.SharedUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.luckynick.custom.Utils.Log;

public class StringPopulator extends CustomJFrame implements ActionListener {

    public static final String LOG_TAG = "StringPopulator";

    private JList leftList;
    DefaultListModel<String> leftListModel = new DefaultListModel<>();
    JTextField newStringField = new JTextField("", 30);;

    public StringPopulator() {
        super("Put strings");
        addElements();
        displayWindow();
    }

    @Override
    protected void addElements() {
        leftList = new JList(leftListModel);

        JScrollPane leftSPane = new JScrollPane();
        leftSPane.getViewport().add(leftList);

        JPanel selectorsPannel = new JPanel();
        selectorsPannel.setLayout(new BoxLayout(selectorsPannel, BoxLayout.X_AXIS));
        selectorsPannel.add(leftSPane);

        JButton selectButton = new JButton("<");
        selectButton.addActionListener(this);
        selectorsPannel.add(selectButton);

        selectorsPannel.add(newStringField);

        getContentPane().add(selectorsPannel);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        getContentPane().add(buttonPanel);
    }



    /**
     *
     * @return null if serialization doesn't have to be done
     */
    public static List<String> requireStrings() {
        StringPopulator manager = new StringPopulator();
        SwingWorker<List<String>, List<String>> sw = new SwingWorker<List<String>, List<String>>() {

            @Override
            protected List<String> doInBackground() throws Exception {
                Thread t = new Thread(manager, "StringPopulator thread");
                t.start();
                try {
                    t.join();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                List<String> result = new ArrayList<>();
                DefaultListModel<String> resultListModel = manager.leftListModel;
                for(int i = 0; i < resultListModel.size(); i++) {
                    result.add(manager.leftListModel.get(i));
                }
                Log(LOG_TAG, "Returning strings (size " + result.size() + ").");
                return result;
            }
        };
        sw.execute();
        try {
            return sw.get();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() instanceof JButton) {
            if(e.getActionCommand().equals("Save")) {
                dispose();
            }
            else if(e.getActionCommand().equals("Cancel")) {
                //selection = null;
                leftListModel.removeAllElements();
                dispose();
            }
            else if(e.getActionCommand().equals("<")) {
                leftListModel.addElement(newStringField.getText());
                newStringField.setText("");
            }
        }
    }
}
