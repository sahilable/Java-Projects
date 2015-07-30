/*
 * DoIt.java
 */

package src.download;

import download.core.downloadmanager.ProtocolWrapperFactory;
import download.gui.downloadmanager.DownloadManagerGUIConstants;
import download.gui.downloadmanager.DownloadManager;
import download.gui.MainFrame;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLClassLoader;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.ArrayList;
import javax.swing.UIManager;

/**
 *
 * @author Rajkumar Nagapuri
 * @version 1.0
 */
public class DoIt
{
    private static URLClassLoader speedDemonClassLoader;

    /** Creates new DoIt */
    private DoIt()
    {
    }//end constr

    private static void setGlobalConstants() throws Exception
    {
        Properties properties = new Properties();
        FileInputStream fis = new FileInputStream(GlobalConstants.DO_IT_PROPERTIES_FILE_NAME);
        properties.load(fis);

        GlobalConstants.DO_IT_HOME_DIRECTORY = new File( (String) properties.get("DO_IT_HOME_DIRECTORY") );
        GlobalConstants.DO_IT_SERVER_PORT = Integer.parseInt( (String) properties.get("SERVER_PORT") );
        DownloadManagerGUIConstants.CHUNK_SIZE = Integer.parseInt( (String) properties.get("CHUNK_SIZE") );

        GlobalConstants.PROTOCOL_WRAPPERS_DIRECTORY = new File(GlobalConstants.DO_IT_HOME_DIRECTORY, "pw");
        GlobalConstants.EXTERNAL_LIBRARIES_DIRECTORY = new File(GlobalConstants.DO_IT_HOME_DIRECTORY, "ext");
        GlobalConstants.MAIN_DOWNLOAD_DIRECTORY = new File(GlobalConstants.DO_IT_HOME_DIRECTORY, "dwnld");

        fis.close();
    }//end method

    /**
     *  @returns True if this is the only instance started. False, if there
     *           is already another instance running.
     */
    private static boolean checkAndStartServer()
    {
        try
        {
            Socket clientSocket = new Socket("localhost", GlobalConstants.DO_IT_SERVER_PORT);
            // another instance running
            return false;
        }//end try
        catch(Exception e)
        { }//end catch

        Thread serverThread = new Thread()
        {
            public void run()
            {
                while(true)
                {
                    try
                    {
                        ServerSocket serverSocket = new ServerSocket(GlobalConstants.DO_IT_SERVER_PORT, 1);
                        while(true)
                        {
                            try
                            {
                                Socket clientSocket = serverSocket.accept();
                                clientSocket.close();
                            }//end try
                            catch(Exception e)
                            { e.printStackTrace(); }//end catch
                        }//end while
                    }//end try
                    catch(Exception e)
                    {  }//end catch
                }//end while
            }//end run
        }; //end thread

        serverThread.setDaemon(true);
        serverThread.start();
        return true;
    }//end method

    private static void loadJARs() throws Exception
    {
        File[] externalJARFiles = GlobalConstants.EXTERNAL_LIBRARIES_DIRECTORY.listFiles();
        File[] protocolWrapperJARFiles = GlobalConstants.PROTOCOL_WRAPPERS_DIRECTORY.listFiles();
        ArrayList arrayURLs = new ArrayList( externalJARFiles.length + protocolWrapperJARFiles.length );

        for(int i=0; i < externalJARFiles.length; i++)
        {
            try
            {
                arrayURLs.add( externalJARFiles[i].toURL() );
            }//end try
            catch(Exception e)
            { e.printStackTrace(); }//end catch
        }//end for
        for(int i=0; i < protocolWrapperJARFiles.length; i++)
        {
            try
            {
                arrayURLs.add( protocolWrapperJARFiles[i].toURL() );
            }//end try
            catch(Exception e)
            { e.printStackTrace(); }//end catch
        }//end for

        URL[] urls = new URL[ arrayURLs.size() ];
        for(int i=0; i < urls.length; i++)
        {
            urls[i] = (URL) arrayURLs.get( i );
        }//end for

        speedDemonClassLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
        Thread.currentThread().setContextClassLoader(speedDemonClassLoader);
    }//end method

    public static ClassLoader getSpeedDemonClassLoader()
    {
        return speedDemonClassLoader;
    }//end method

    public static void exitSpeedDemon(int status)
    {
        System.exit(status);
    }//end method

    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        try
        {
            setGlobalConstants();
            if( ! checkAndStartServer() )
                throw new Exception("You cannot run more than one instance.");

            loadJARs();
            ProtocolWrapperFactory.init();

            try
            {
                UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
            }//end try
            catch(Exception e)
            {  }//end catch

            MainFrame.createMainFrame();
            DownloadManager.init();
        }//end try
        catch(Exception e)
        {
            e.printStackTrace();
            System.exit( 1 );
        }//end catch
    }//end main

}//end class
