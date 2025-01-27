package cz.hudecekpetr.snowride.ui.about;

import cz.hudecekpetr.snowride.ui.Images;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;
import java.net.URI;


public class AboutSnowride extends AboutDialogBase {

    public static final String GITHUB_MAIN_URL = "https://github.com/Soothsilver/snowride";

    public AboutSnowride() {
        Label lblSnowride = new Label("Snowride");
        lblSnowride.setFont(MainForm.BIGGER_FONT);
        Label lblDescription = new Label("Snowride is a fast and many-featured IDE for Robot Framework test projects.");
        Label lblAuthor = new Label("© 2019 Petr Hudeček");
        Label lblAuthor2 = new Label("© 2019 ION");
        Label lblLicense = new Label("Distributed under GNU GPL v3");
        Hyperlink hyperlinkToGithub = new Hyperlink(GITHUB_MAIN_URL);
        hyperlinkToGithub.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Desktop.getDesktop().browse(new URI(GITHUB_MAIN_URL));
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        VBox vbMain = new VBox(5, lblSnowride, lblDescription, lblAuthor, lblAuthor2, lblLicense, hyperlinkToGithub);
        Button bClose = new Button("Close");
        bClose.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                close();
            }
        });
        HBox hButtons = new HBox(5, bClose);
        hButtons.setAlignment(Pos.CENTER_RIGHT);
        VBox vbAll = new VBox(vbMain, hButtons);
        VBox.setVgrow(vbMain, Priority.ALWAYS);
        vbAll.setPadding(new Insets(5));
        this.setScene(new Scene(vbAll, 500, 400));
        this.setTitle("About Snowride");
        this.getIcons().add(Images.snowflake);
    }
}
