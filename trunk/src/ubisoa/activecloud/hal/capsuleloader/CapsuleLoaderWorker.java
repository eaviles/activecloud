package ubisoa.activecloud.hal.capsuleloader;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.log4j.Logger;
import org.jdesktop.swingworker.SwingWorker;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import ubisoa.activecloud.hal.capsules.ICapsule;

public class CapsuleLoaderWorker extends SwingWorker<List<ICapsule>, String>{
	private static Logger log = Logger.getLogger(CapsuleLoaderWorker.class);
	private JPanel viewer;
	private JPanel configUI;
	private JProgressBar progressBar;
	private String[] filenames;
	
	public CapsuleLoaderWorker(JPanel viewer, JProgressBar progressBar, 
			JPanel configUI, String... filenames){
		this.viewer = viewer;
		this.configUI = configUI;
		this.progressBar = progressBar;
		this.filenames = filenames;
	}
	
	//In the EDT
	@Override
	protected void done(){
		try{
			for(final ICapsule capsule : get()){
				JLabel capsuleLabel = new JLabel(new ImageIcon(capsule.getIcon()));
				capsuleLabel.addMouseListener(new MouseListener(){
					@Override
					public void mouseClicked(MouseEvent arg0) {
						// TODO Auto-generated method stub
						configUI.add(capsule.getConfigUI(), BorderLayout.CENTER);
						configUI.revalidate();
						log.debug("loaded configUI");
					}

					@Override
					public void mouseEntered(MouseEvent arg0) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void mouseExited(MouseEvent arg0) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void mousePressed(MouseEvent arg0) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void mouseReleased(MouseEvent arg0) {
						// TODO Auto-generated method stub
						
					}
					
				});
				viewer.add(capsuleLabel);
				viewer.revalidate();
				progressBar.setValue(0);
			}
		} catch(Exception e) {
			log.error(e.getMessage());
			JOptionPane.showMessageDialog(null, "A capsule was detected but not loaded.\n" +
					"Possible reasons include incomplete or malformed config.xml.", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	// In the EDT
	@Override
	protected void process(List<String> messages) {
		for (String message : messages) {
			log.info(message+"\n");
		}
	}

	//This runs in a background thread
	@Override
	protected List<ICapsule> doInBackground() throws Exception {
		List<ICapsule> capsules = new ArrayList<ICapsule>();
		progressBar.setMaximum(filenames.length);
		log.debug("Max Progress Value: "+filenames.length);
		progressBar.setValue(0);
		int n = 0;
		for(String filename : filenames){
			try{
				CapsuleLoader loader = new CapsuleLoader();
				/*The given filenames are those of Capsules, we need to extract
				 * the image file and return it as a list. By convention the
				 * image file must be named icon.png*/
				JarFile jar = new JarFile(new File(filename));
				SAXBuilder builder = new SAXBuilder();
				Document doc = builder.build(jar.getInputStream(
						new ZipEntry("config.xml")));
				Element root = doc.getRootElement();
				
				//Add the capsule to the classpath
				ClassPathHacker.addFile(filename);
				
				/*Create the capsule object representation
				 * from the files previously readed*/
				ICapsule capsule = (ICapsule)loader.initClass("capsule", ICapsule.class, root);
				
				/*A null capsule can be returned if that capsule is already
				 * loaded*/
				if(capsule != null){
					capsule.setIcon(ImageIO.read(jar.getInputStream(new ZipEntry("icon.png"))));
					capsule.setConfigElement(root);
					capsules.add(capsule);
				}
	
				
				/*Publish the temporary results*/
				publish("Loaded " + filename);
				n++;
				log.debug("Progress Value: "+n);
				progressBar.setValue(n);
			} catch (IOException ioe) {
				log.error(ioe.getMessage());
				JOptionPane.showMessageDialog(null, ioe.getMessage(), 
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		return capsules;
	}
}