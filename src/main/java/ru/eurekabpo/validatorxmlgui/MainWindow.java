package ru.eurekabpo.validatorxmlgui;

import ru.eurekabpo.validatorxmlutil.Main;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainWindow {
  private final JFrame frame;
  private JProgressBar progressBar;
  private JButton validateButton;
  private JButton chooseFileButton;
  private JButton cancelValidateButton;
  private JButton reportButton;
  private JFileChooser fileChooser;
  private JLabel message;
  private ValidationTask validationTask;
  private File reportFile;

  public MainWindow() {
    this.frame = new JFrame("Валидация досье");
    frame.setSize(400, 300);
    frame.setLayout(null);
    frame.setResizable(false);
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        if (reportFile != null) {
          deleteAllFilesFolder(reportFile.getParent());
        }
        System.exit(0);
      }
    });

    initValidateButton();
    initCancelValidateButton();
    initReportButton();
    initFileChooser();
    initProgressBar();
    initMessageLabel();
  }

  public void show() {
    centreWindow(frame);
    frame.setVisible(true);
  }

  private void initProgressBar() {
    progressBar = new JProgressBar();
    progressBar.setBounds(100, 90, 200, 20);
    progressBar.setIndeterminate(false);
    progressBar.setVisible(false);
    this.frame.add(progressBar);
  }

  public void initReportButton() {
    reportButton = new JButton();
    reportButton.setBounds(75, 200, 250, 30);
    reportButton.setVisible(false);
    reportButton.setText("Скачать протокол валидации");
    JFileChooser saveFile = new JFileChooser();
    reportButton.addActionListener(arg -> {
      try {
        saveFile.setDialogTitle("Сохранение файла");
        saveFile.setFileSelectionMode(JFileChooser.FILES_ONLY);
        saveFile.setFileHidingEnabled(true);
        int result = saveFile.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION && saveFile.getSelectedFile() != null) {
          Files.copy(Path.of(reportFile.getAbsolutePath()),
            Path.of(saveFile.getSelectedFile().getAbsolutePath() + ".xls"),
            StandardCopyOption.REPLACE_EXISTING);
          JOptionPane.showMessageDialog(frame,
            "Файл '" + saveFile.getSelectedFile() + ".xls" +
              " ) сохранен");
          Desktop.getDesktop().open(new File(saveFile.getSelectedFile().getAbsolutePath() + ".xls"));
        }
      } catch (HeadlessException | IOException e) {
        throw new RuntimeException(e);
      }
    });
    this.frame.add(reportButton);
  }

  private void initFileChooser() {
    this.fileChooser = new JFileChooser();
    this.fileChooser.setFileFilter(new FileFilter() {
      public String getDescription() {
        return "XML documents (*.xml)";
      }

      public boolean accept(File f) {
        if (f.isDirectory()) {
          return true;
        } else {
          String filename = f.getName().toLowerCase();
          return filename.endsWith(".xml");
        }
      }
    });
    chooseFileButton = new JButton("Выбрать файл");
    chooseFileButton.setBounds(100, 50, 200, 30);
    JLabel selectedFileName = new JLabel();
    selectedFileName.setBounds(100, 25, 200, 30);
    this.frame.add(selectedFileName);
    this.frame.add(chooseFileButton);

    chooseFileButton.addActionListener(arg -> {
      this.fileChooser.showOpenDialog(null);
    });

    fileChooser.addActionListener(arg -> {
      if (fileChooser.getSelectedFile() != null) {
        selectedFileName.setText(fileChooser.getSelectedFile().getName());
        selectedFileName.setToolTipText(fileChooser.getSelectedFile().getName());
        reportButton.setVisible(false);
        message.setVisible(false);
      }
    });
  }

  private void initCancelValidateButton() {
    cancelValidateButton = new JButton("Отмена");
    cancelValidateButton.setBounds(125, 125, 150, 30);
    cancelValidateButton.setVisible(false);
    frame.add(cancelValidateButton);
    cancelValidateButton.addActionListener(arg -> {
      validationTask.stop();
      progressBar.setIndeterminate(false);
      chooseFileButton.setEnabled(true);
      validateButton.setVisible(true);
      progressBar.setVisible(false);
      frame.setCursor(null);
    });
  }

  private void initValidateButton() {
    validateButton = new JButton("Валидация");
    validateButton.setBounds(125, 125, 150, 30);
    this.frame.add(validateButton);
    validateButton.addActionListener(arg -> {
      if (fileChooser.getSelectedFile() != null) {
        cancelValidateButton.setVisible(true);
        validateButton.setVisible(false);
        reportButton.setEnabled(true);
        reportButton.setVisible(false);
        progressBar.setVisible(true);
        message.setVisible(false);
        chooseFileButton.setEnabled(false);
        progressBar.setIndeterminate(true);
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        validationTask = new ValidationTask();
        validationTask.start();
      } else {
        JOptionPane.showMessageDialog(null, "Выберите файл!");
        progressBar.setIndeterminate(false);
      }
    });
  }

  private void initMessageLabel() {
    this.message = new JLabel();
    message.setVisible(false);
    this.frame.add(message);
  }

  private void centreWindow(JFrame frame) {
    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
    int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
    frame.setLocation(x, y);
  }

  public String[] getPathToUtilExec() {
    String currentRelativePath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    List<String> paramArray = new ArrayList<>();
    System.out.println(currentRelativePath);
    String javaPath = System.getProperty("java.home");
    if (System.getProperty("os.name").startsWith("Windows")){
      currentRelativePath = currentRelativePath.substring(1);
      paramArray.add(javaPath + File.separator + "bin" + File.separator + "java.exe");
    }else {
      paramArray.add(javaPath + File.separator + "bin" + File.separator + "java");
    }

    if (currentRelativePath.endsWith(".jar")) {
      paramArray.add("-cp");
      paramArray.add(currentRelativePath);
      paramArray.add("ru.eurekabpo.validatorxmlutil.Main");
      paramArray.add("-file=" + fileChooser.getSelectedFile().getAbsolutePath());
    } else {
      paramArray.add("-jar" + currentRelativePath + "jar/ValidatorXMLUtil.jar");
      paramArray.add("-file=" + fileChooser.getSelectedFile().getAbsolutePath());
    }

    return paramArray.toArray(new String[0]);
  }

  private class ValidationTask extends Thread {
    @Override
    public void run() {
//      try {
//        Main.main(new String[]{"-file=" + fileChooser.getSelectedFile().getAbsolutePath()});
//      } catch (Exception e) {
//        throw new RuntimeException(e);
//      }
      try {
        Process process = Runtime.getRuntime().exec(getPathToUtilExec());
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String s;
        while ((s = in.readLine()) != null) {
          System.out.println(s);
          if (s.startsWith("[REPORT]")) {
            reportFile = new File(s.replace("[REPORT] ", ""));
          }
        }
        int status = process.waitFor();
        if (status == 0) {
          message.setText("Файл корректный");
          message.setBounds(135, 150, 250, 30);
        } else {
          message.setText("Файл не прошёл валидацию");
          message.setBounds(100, 150, 250, 30);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      validateButton.setVisible(true);
      cancelValidateButton.setVisible(false);
      reportButton.setVisible(true);
      if (reportFile == null) {
        reportButton.setEnabled(false);
      }
      chooseFileButton.setEnabled(true);
      frame.setCursor(null);
      message.setVisible(true);
      progressBar.setVisible(false);
      progressBar.setIndeterminate(false);
    }
  }

  public static void deleteAllFilesFolder(String path) {
    for (File myFile : new File(path).listFiles())
      if (myFile.isFile()) myFile.delete();
  }
}
