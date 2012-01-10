package pl.elka.gis.ui.components;

import pl.elka.gis.logic.GraphResolver;

public interface ProgressCallback {

    public void updateProgress(float progressValue);

    public void increaseProgress(float progressToAdd);

    public void calculationError(String errorMessage);

    public void calculationFinished(GraphResolver.Result result);

    public void calculationStopped();
}
