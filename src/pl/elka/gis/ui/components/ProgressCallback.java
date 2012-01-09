package pl.elka.gis.ui.components;

public interface ProgressCallback {

    public void updateProgress(float progressValue);

    public void increaseProgress(float progressToAdd);

    public void calculationError(String errorMessage);

    public void calculationFinished();
}
