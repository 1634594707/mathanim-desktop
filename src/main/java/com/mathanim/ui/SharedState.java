package com.mathanim.ui;

import com.mathanim.domain.RenderJob;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.springframework.stereotype.Component;

/**
 * Shared JavaFX state between split child controllers. MainViewController remains the coordinator
 * for now, while child controllers consume shared selection state without tight coupling.
 */
@Component
public class SharedState {

  private final ObjectProperty<RenderJob> selectedJob = new SimpleObjectProperty<>();

  public ObjectProperty<RenderJob> selectedJobProperty() {
    return selectedJob;
  }

  public RenderJob getSelectedJob() {
    return selectedJob.get();
  }

  public void setSelectedJob(RenderJob job) {
    selectedJob.set(job);
  }
}
