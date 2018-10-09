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

public class ModelSelector<T extends SerializableModel> extends CustomJFrame implements ActionListener, ListSelectionListener {

    public static final String LOG_TAG = "ModelSelector";

    //private List<T> selection = new ArrayList<>();
    private boolean multipleSelection; //TODO: use
    JLabel nothingToLoadLabel = new JLabel("<html><font color='red'>Nothing to load!</font></html>");

    private JList rightList;
    private JList leftList;
    DefaultListModel<T> leftListModel = new DefaultListModel<>();

    private ModelIO<T> modelIO;

    public ModelSelector(ModelIO<T> modelIO) {
        this(modelIO, false);
    }

    public ModelSelector(ModelIO<T> modelIO, boolean multipleSelection) {
        super("Select " + modelIO.getClassOfModel().getSimpleName());
        this.multipleSelection = multipleSelection;
        this.modelIO = modelIO;

        addElements();
        displayWindow();
    }

    @Override
    protected void addElements() {
        List<T> modelObjects = modelIO.listObjects();

        leftList = new JList(leftListModel);

        rightList = new JList(SharedUtils.toArray(modelObjects));
        //rightList.addListSelectionListener(this);

        JScrollPane leftSPane = new JScrollPane();
        leftSPane.getViewport().add(leftList);

        JScrollPane rightSPane = new JScrollPane();
        rightSPane.getViewport().add(rightList);

        JPanel selectorsPannel = new JPanel();
        selectorsPannel.setLayout(new BoxLayout(selectorsPannel, BoxLayout.X_AXIS));

        selectorsPannel.add(leftSPane);
        JButton selectButton = new JButton("<");
        selectButton.addActionListener(this);
        selectorsPannel.add(selectButton);
        selectorsPannel.add(rightSPane);

        getContentPane().add(selectorsPannel);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        getContentPane().add(buttonPanel);

        JPanel errorPanel = new JPanel();
        errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
        nothingToLoadLabel.setVisible(modelObjects.size() == 0);
        errorPanel.add(nothingToLoadLabel);
        getContentPane().add(errorPanel);
    }



    /**
     *
     * @param <T>
     * @return null if serialization doesn't have to be done
     */
    public static <T extends SerializableModel> List<T> requireSelection(ModelIO<T> modelIO, boolean multipleSelection) {
        ModelSelector<T> manager = new ModelSelector<>(modelIO, multipleSelection);
        SwingWorker<List<T>, List<T>> sw = new SwingWorker<List<T>, List<T>>() {

            @Override
            protected List<T> doInBackground() throws Exception {
                Thread t = new Thread(manager, "ModelEditor thread");
                t.start();
                try {
                    t.join();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //return manager.selection;
                List<T> result = new ArrayList<>();
                DefaultListModel<T> resultListModel = manager.leftListModel;
                for(int i = 0; i < resultListModel.size(); i++) {
                    result.add(manager.leftListModel.get(i));
                }
                Log(LOG_TAG, "Returning selection (size " + result.size() + ").");
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
                List<T> checked = rightList.getSelectedValuesList();
                if(multipleSelection) {
                    for(T itemToAdd : checked) {
                        leftListModel.addElement(itemToAdd);
                    }
                }
                else {
                    leftListModel.removeAllElements();
                    if(checked.size() > 0) {
                        leftListModel.addElement(checked.get(0));
                    }
                }
            }
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            List<T> checked = rightList.getSelectedValuesList();
            System.out.println(checked.size() + " values selected");
            //selection = checked;

            leftListModel.removeAllElements();
            /*for(T obj: selection) {
                leftListModel.addElement(obj);
            }*/
        }
    }
}
