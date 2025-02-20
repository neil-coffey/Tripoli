/*
 * Copyright 2022 James Bowring, Noah McLean, Scott Burdick, and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cirdles.tripoli.gui;

import jakarta.xml.bind.JAXBException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.cirdles.tripoli.sessions.Session;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.utilities.IntuitiveStringComparator;

import java.net.URL;
import java.util.ResourceBundle;

import static org.cirdles.tripoli.constants.ConstantsTripoliCore.MISSING_STRING_FIELD;
import static org.cirdles.tripoli.gui.TripoliGUIController.tripoliSession;
import static org.cirdles.tripoli.gui.constants.ConstantsTripoliApp.TRIPOLI_SESSION_LINEN;
import static org.cirdles.tripoli.gui.constants.ConstantsTripoliApp.convertColorToHex;

/**
 * @author James F. Bowring
 */
public class SessionManagerController implements Initializable {
    public ColumnConstraints columnTwoConstraints;
    public GridPane sessionGridPane;
    @FXML
    private TextField analystNameText;
    @FXML
    private TextField sessionNameText;
    @FXML
    private TextArea sessionNotesText;
    @FXML
    private TextField sessionFilePathAsStringText;
    @FXML
    private ListView<AnalysisInterface> listViewOfAnalyses;

    /**
     * @param location  The location used to resolve relative paths for the root object, or
     *                  {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if
     *                  the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (null == tripoliSession) {
            try {
                tripoliSession = Session.initializeDefaultSession();
            } catch (JAXBException e) {
                //
            }
        }

        sessionGridPane.setStyle("-fx-background-color: " + convertColorToHex(TRIPOLI_SESSION_LINEN));

        populateSessionManagerGridPane();
        setupListeners();
    }

    private void populateSessionManagerGridPane() {
        sessionNameText.setEditable(tripoliSession.isMutable());
        sessionNameText.setText(tripoliSession.getSessionName());

        analystNameText.setEditable(tripoliSession.isMutable());
        analystNameText.setText(tripoliSession.getAnalystName());

        sessionFilePathAsStringText.setEditable(false);
        sessionFilePathAsStringText.setText(tripoliSession.getSessionFilePathAsString());

        ObservableList<AnalysisInterface> items = FXCollections.observableArrayList(tripoliSession.getMapOfAnalyses().values());
        listViewOfAnalyses.setCellFactory((parameter) -> new AnalysisDisplaySummary());
        IntuitiveStringComparator<String> intuitiveStringComparator = new IntuitiveStringComparator<>();
        items = items.sorted((AnalysisInterface analysis1, AnalysisInterface analysis2)
                -> intuitiveStringComparator.compare(analysis1.getAnalysisName(), analysis2.getAnalysisName()));
        listViewOfAnalyses.setItems(items);
        listViewOfAnalyses.setOnMouseClicked(event -> {
            if (MouseButton.PRIMARY == event.getButton()) {
                if (2 == event.getClickCount() && -1 == event.getTarget().toString().lastIndexOf("null")) {
                    AnalysisInterface analysisSelected = ((AnalysisInterface) ((ListView) event.getSource()).getSelectionModel().getSelectedItem());
                    AnalysisManagerController.analysis = analysisSelected;
                    MenuItem menuItemAnalysesManager = ((MenuBar) TripoliGUI.primaryStage.getScene()
                            .getRoot().getChildrenUnmodifiable().get(0)).getMenus().get(1).getItems().get(0);
                    menuItemAnalysesManager.fire();
                }
            }
        });
        if (0 < items.size()) {
            listViewOfAnalyses.getSelectionModel().selectFirst();
            AnalysisManagerController.analysis = listViewOfAnalyses.getSelectionModel().getSelectedItem();
        }
    }

    private void setupListeners() {
        sessionNameText.textProperty().addListener((observable, oldValue, newValue) -> {
            assert null != tripoliSession;
            tripoliSession.setSessionName(newValue.isBlank() ? MISSING_STRING_FIELD : newValue);
        });
        analystNameText.textProperty().addListener((observable, oldValue, newValue) -> {
            assert null != tripoliSession;
            tripoliSession.setAnalystName(newValue.isBlank() ? MISSING_STRING_FIELD : newValue);
        });
        sessionNotesText.textProperty().addListener((observable, oldValue, newValue) -> {
            assert null != tripoliSession;
            tripoliSession.setSessionNotes(newValue.isBlank() ? MISSING_STRING_FIELD : newValue);
        });
    }

    static class AnalysisDisplaySummary extends ListCell<AnalysisInterface> {
        @Override
        protected void updateItem(AnalysisInterface analysis, boolean empty) {
            super.updateItem(analysis, empty);
            if (null == analysis || empty) {
                setText(null);
            } else {
                setText(analysis.prettyPrintAnalysisSummary());
                setFont(Font.font("Monospaced", FontWeight.EXTRA_BOLD, 12));
            }
        }
    }

}