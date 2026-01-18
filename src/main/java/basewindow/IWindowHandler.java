package basewindow;

public interface IWindowHandler 
{
	boolean attemptCloseWindow();

	void onWindowClose();

    void onFilesDropped(String... filePaths);
}
