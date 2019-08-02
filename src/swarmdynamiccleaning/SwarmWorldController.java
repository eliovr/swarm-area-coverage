package swarmdynamiccleaning;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

/**
 *
 * @author Elio Ventocilla
 */
public class SwarmWorldController extends SwarmWorld implements Initializable{

    @FXML
    private AnchorPane worldPane;

    @FXML
    private Slider slrAgentsWidth;

    @FXML
    private Label lblAgentsWidth;

    @FXML
    private Slider slrAgentsNumber;

    @FXML
    private Label lblAgentsNumber;

    @FXML
    private Button btnStartPause;

    @FXML
    private Button btnRefresh;

    @FXML
    private Slider slrFps;

    @FXML
    private Label lblFps;

    @FXML
    private Slider slrPersonalRange;

    @FXML
    private Label lblPersonalRange;

    @FXML
    private Slider slrComfortRange;

    @FXML
    private Label lblComfortRange;

    @FXML
    private Slider slrFlockRange;

    @FXML
    private Label lblFlockRange;

    @FXML
    private Slider slrStepSize;

    @FXML
    private Label lblStepSize;

    @FXML
    private CheckBox chkGrid;

    @FXML
    private Label lblFilledSpace;

    @FXML
    private Label lblExecutedFrames;

    @FXML
    private Slider slrPheromone;

    @FXML
    private Label lblPheromone;

    @FXML
    private Slider slrEvaporation;

    @FXML
    private Label lblEvaporation;

    @FXML
    private Slider slrInfluence;

    @FXML
    private Label lblInfluence;

    @FXML
    private Slider slrInertia;

    @FXML
    private Label lblInertia;

//    @FXML
//    private Button btnSaveCurrent;
//
//    @FXML
//    private Button btnLoad;

    @FXML
    private ComboBox<World> cbWorlds;

    private boolean cellUnderMouse;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Add event listener to the fps slider. Tried to do it the same way as the other listeners but failed.
        slrAgentsNumber.valueProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                lblAgentsNumber.setText(String.valueOf(newValue.intValue()));
                updateFps();
            }
        });

        slrAgentsWidth.valueProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                lblAgentsWidth.setText(String.valueOf(newValue.intValue()));
            }
        });

        slrFps.valueProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                lblFps.setText(String.valueOf(newValue.intValue()));
                updateFps();
            }
        });

        slrPersonalRange.valueProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                lblPersonalRange.setText(String.valueOf(newValue.intValue()));
            }
        });

        slrComfortRange.valueProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                lblComfortRange.setText(String.valueOf(newValue.intValue()));
            }
        });

        slrFlockRange.valueProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                lblFlockRange.setText(String.valueOf(newValue.intValue()));
            }
        });

        slrStepSize.valueProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                lblStepSize.setText(String.valueOf(newValue.intValue()));
            }
        });

        slrPheromone.valueProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                lblPheromone.setText(String.valueOf(newValue.intValue()));
            }
        });

        slrEvaporation.valueProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                lblEvaporation.setText(String.valueOf(newValue.intValue()));
            }
        });

        slrInfluence.valueProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                lblInfluence.setText(String.valueOf(newValue.intValue()));
            }
        });

        slrInertia.valueProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                lblInertia.setText(String.valueOf(newValue.intValue()));
            }
        });

        cbWorlds.setItems(FXCollections.observableList(getWorlds()));
        cbWorlds.getSelectionModel().selectLast();
    }

    // ====================== EVENT HANDLERS ============================
    @FXML
    private void handleStartPauseButtonAction(ActionEvent event) {
        if (!isRunning()){
            this.play();
            btnStartPause.setText("Pause");
        }
        else{
            this.pause();
            btnStartPause.setText("Start");
        }
    }

    @FXML
    private void handleRefreshButtonAction(ActionEvent event) {
        this.refresh();
        btnStartPause.setText("Start");
    }

    @FXML
    private void handleChkGridAction(ActionEvent event) {
        this.showHideGrid();
    }

    @FXML
    private void onMousePressed(MouseEvent event) {
        if (!isRunning()){
            cellUnderMouse = isWall(new Point2D(event.getX(), event.getY()));
            addRemoveWallAt(new Point2D(event.getX(), event.getY()), !cellUnderMouse);
        }
    }

    @FXML
    private void onMouseReleased(MouseEvent event) {
        Point2D pos = new Point2D(event.getX(), event.getY());

        if (!isRunning())
            addRemoveWallAt(pos, !cellUnderMouse);
        else
            addLeakAt(pos);
    }

    @FXML
    private void onMouseDragged(MouseEvent event) {
        if (!isRunning())
            addRemoveWallAt(new Point2D(event.getX(), event.getY()), !cellUnderMouse);
    }

//    @FXML
//    private void handleSaveCurrentButtonAction(ActionEvent event) {
//        this.saveWorlds();
//
//    }

//    @FXML
//    private void handleLoadButtonAction(ActionEvent event) {
//        this.loadSelectedWorld();
//        btnStartPause.setText("Start");
//    }

    // ==================== SWARM WOLRD OVERRID METHODS ======================

    @Override
    protected int getFramesPerSecond() {
        return (int)slrFps.getValue();
    }

    @Override
    protected Pane getWorldPane() {
        return worldPane;
    }

    @Override
    protected int getNumberOfAgents() {
        return (int)slrAgentsNumber.getValue();
    }

    @Override
    protected double getAgentsSize() {
        return slrAgentsWidth.getValue();
    }

    @Override
    protected boolean displayGrid() {
        return chkGrid.isSelected();
    }

    @Override
    protected void updateFilledSpace(int percentage) {
        lblFilledSpace.setText(String.valueOf(percentage));
    }

    @Override
    protected void updateExecutedFrames(int frames) {
        lblExecutedFrames.setText(String.valueOf(frames));
    }

    @Override
    protected double getPersonalRange() {
        return slrPersonalRange.getValue();
    }

    @Override
    protected double getComfortRange() {
        return slrComfortRange.getValue();
    }

    @Override
    protected double getFlockRange() {
        return slrFlockRange.getValue();
    }

    @Override
    protected double getStepSize() {
        return slrStepSize.getValue();
    }

    @Override
    protected double getPheromone() {
        return slrPheromone.getValue();
    }

    @Override
    protected double getEvaporation() {
        return slrEvaporation.getValue();
    }

    @Override
    protected double getInfluence() {
        return slrInfluence.getValue();
    }

    @Override
    protected double getInertia() {
        return slrInertia.getValue();
    }

    @Override
    protected World getSelectedWorld() {
        return cbWorlds.getValue();
    }
}
