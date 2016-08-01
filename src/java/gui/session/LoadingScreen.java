package gui.session;

/**
 * Created by root on 29.7.16.
 */
public interface LoadingScreen {

    public void startLoading(int maximum);

    public void addProgress(int amount);

    public void setText(String text);

    public void stopLoading();
}
