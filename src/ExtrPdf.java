import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;



public class ExtrPdf {

	private int imageCounter;
	
//	private List<File> fileList = new ArrayList<File>();
	final static File FOLDER = new File("/Users/sephon/Desktop/Research/ReVision/pdf/");
	final static int THRESHOLD = 80; //size threshold
	
	final static String ALL_IN_ONE_FOLDER = "allimages/";
	final static String OUTFOLDER = "/Users/sephon/Desktop/Research/ReVision/image_output/";
	final static boolean ALL_IN_ONE_DIR = true;
	
//	 listFilesForFolder(folder);
	
	
	public static void main(String[] args) throws Exception {
		
		
		ExtrPdf ImageExtractor = new ExtrPdf();
		ImageExtractor.ExtrPdf(args);

		
	}
	
	private ExtrPdf() {}
	
	private void ExtrPdf(String[] args) throws Exception {

		imageCounter = 1;
		
		ArrayList<File> fileList = new ArrayList<File>();
		fileList = listPdfFilesForFolder(FOLDER, fileList);

		for (File f : fileList) {
			try {
				File output = new File(OUTFOLDER);
				output.mkdir();
				readPdf(f);
			}
			catch (IOException ex)
			{
				System.out.println("" + ex);
			}
		}
		
//		try {
//			System.out.println("start");
////			Read_pdf(new File("pdf/revision.pdf"));
//			Read_pdf();
//		}
//		catch (IOException ex)
//		{
//			System.out.println("" + ex);
//		}
		
		
		
	}
	

	

	
	
	
	////////
	public void readPdf(File f) throws IOException {
		PDDocument document = null; 
//		System.out.println("Start Image Extraction");
		try {
			document = PDDocument.load(f.getPath());
		}
		catch (IOException ex) {
			System.out.println("" + ex);
		}
		
		// make a new directory by the name of input file
		if (!ALL_IN_ONE_DIR) {
			File dir = new File(OUTFOLDER + f.toString());
			makeDir(dir);
		}
//		System.out.println(dir.toString());
		
		System.out.println("Start image extraction from " + f.getPath());
		List pages = document.getDocumentCatalog().getAllPages();
		Iterator iter = pages.iterator(); 
		// prefix = "<pdf filename>/image"
		String prefix;
		if (ALL_IN_ONE_DIR) {
			prefix = ALL_IN_ONE_FOLDER + "image";
		}else {
			prefix = getFilePrefix(f) + "/" + "image";
		}
//		String prefix = getFilePrefix(f) + "/" + "image";
//		String prefix = "allimages/image";
		
		while (iter.hasNext()) {
			PDPage page = (PDPage) iter.next();
			PDResources resources = page.getResources();
			processResources(resources, prefix);
		}
		
		System.out.println("write " + getFilePrefix(f) + "'s profile");
		writeProfile(getFilePrefix(f), imageCounter);
		
		// reset image counter to 1
		if (!ALL_IN_ONE_DIR) imageCounter = 1;
		
		document.close();
		System.out.println("All images were extracted from " + f.getPath());
		System.out.println();
	}
	
	public void writeProfile(String title, int numOfImg) {
		try {
			BufferedWriter out;
			if (ALL_IN_ONE_DIR) {
				File newDir = new File(OUTFOLDER + ALL_IN_ONE_FOLDER);
				boolean result = newDir.mkdir();  
				if(result) {    
					System.out.println("Directory " + newDir.toString() + " created");
				}
				out = new BufferedWriter(new FileWriter(OUTFOLDER + ALL_IN_ONE_FOLDER + "/" + title + ".txt"));
			}else {
				out = new BufferedWriter(new FileWriter(OUTFOLDER + title + "/" + title + ".txt"));
			}
			out.write("Title, " + title + "\n");
			out.write("# Images, " + numOfImg + "\n");
			out.write("Size Limitation, " + THRESHOLD + "\n");

			out.close();
		} catch (IOException ex) {
			System.out.println("" + ex);
		}
	}
	
	public void Read_pdf() throws IOException {
		PDDocument document = null; 
		System.out.println("Start Image Extraction");
		try
		{
			document = PDDocument.load("revision.pdf");
		}
		catch (IOException ex)
		{
			System.out.println("" + ex);
		}
		
		java.util.List pages = document.getDocumentCatalog().getAllPages();
		Iterator iter = pages.iterator(); 
		int i = 1;
		String name = null;
	
		while (iter.hasNext()) {

			PDPage page = (PDPage) iter.next();
			PDResources resources = page.getResources();
			String prefix = "image";
			processResources(resources, prefix);

			
			
//			Map pageImages = resources.getImages();
//			if (pageImages != null) { 
//				Iterator imageIter = pageImages.keySet().iterator();
//				while (imageIter.hasNext()) {
//					String key = (String) imageIter.next();
//					PDXObjectImage image = (PDXObjectImage) pageImages.get(key);
//					image.write2file("image_" + i);
//					i ++;
//				}
//			}
		}
	}
	
	public boolean isUsefulImg(PDXObjectImage image, int threshold) {
		return (image.getHeight() > threshold) && (image.getWidth() > threshold);
	}
	
	private void processResources(PDResources resources, String prefix) throws IOException {
        if (resources == null) {
            return;
        }
       
        Map<String, PDXObject> xobjects = resources.getXObjects();
        
        
        if( xobjects != null ) {
            Iterator<String> xobjectIter = xobjects.keySet().iterator();
            while( xobjectIter.hasNext() ) {
                String key = xobjectIter.next();
                PDXObject xobject = xobjects.get(key);
                
                // write the images
                if (xobject instanceof PDXObjectImage) {
                    PDXObjectImage image = (PDXObjectImage) xobject;
//                    image.getRGBImage().
                    
                    
                    if(isUsefulImg(image, THRESHOLD)) {
                    	System.out.println("Height = " + image.getHeight() + " Width = " + image.getWidth());
                    	
                    	String name = null;
                    	name = getUniqueFileName( prefix, image.getSuffix() );
                    	System.out.println( "Writing image:" + name );
                    	image.write2file( OUTFOLDER + name );
                    }
                }
                // maybe there are more images embedded in a form object
                else if (xobject instanceof PDXObjectForm) {
                    PDXObjectForm xObjectForm = (PDXObjectForm)xobject;
                    PDResources formResources = xObjectForm.getResources();
                    processResources(formResources, prefix);
                }
            }
        }
    }
	
	
	
	private String getUniqueFileName(String prefix, String suffix ) {
		String uniqueName = null;
		File f = null;
		while( f == null || f.exists() ) {
			uniqueName = prefix + "_" + imageCounter;
			f = new File( uniqueName + "." + suffix );
			imageCounter++;
		}
		return uniqueName;
	}
	
	private static void makeDir(File dirName) {
		if (dirName != null) {
			String directoryName = getFilePrefix(dirName);
			
			// make the folder in output folder
			File newDir = new File(OUTFOLDER + directoryName);
			if (!newDir.exists()) {
				System.out.println("Creating directory: " + newDir.toString() + " ...");
				boolean result = newDir.mkdir();  
				if(result) {    
					System.out.println("Directory " + newDir.toString() + " created");
				}
			}
		}
	}
	
	// return all files in the given folder including files in subdirectories
		public static ArrayList<File> listPdfFilesForFolder(final File folder, ArrayList<File> fileList) {
			File[] filesInFolder = folder.listFiles();
			if (filesInFolder != null) {
				for (final File fileEntry : filesInFolder) {
					if (fileEntry.isDirectory()) {
						fileList = listPdfFilesForFolder(fileEntry,fileList);
					} else {
						if(isPDF(fileEntry)) fileList.add(fileEntry);
					}
				}
				return fileList;
			}
			return fileList;
		}
		
		
		// determine if the input file is a pdf file
		public static boolean isPDF(File file) {
			if (file != null) {
				String fileName = file.getName();
				String extension = fileName.substring(fileName.length() - 3);
				if (extension.toLowerCase().equals("pdf")) return true;
			}
			return false;
		}
	
		public static String getFilePrefix(File file) {
			String fileName = file.getName();
			return fileName.substring(0, fileName.length() - 4);
		}
		

}


//public static void Read_pdf(File f) throws IOException {
//PDDocument document = null; 
//System.out.println("Start Image Extraction");
//try {
//	document = PDDocument.load(f.getPath());
//}
//catch (IOException ex) {
//	System.out.println("" + ex);
//}
//
//// make a new directory by the name of input file
//makeDir(f);
//
//List pages = document.getDocumentCatalog().getAllPages();
//Iterator iter = pages.iterator(); 
//int i =1;
//String name = null;
//
//while (iter.hasNext()) {
//	PDPage page = (PDPage) iter.next();
//	PDResources resources = page.getResources();
//	Map pageImages = resources.getImages();
//	if (pageImages != null) { 
//		Iterator imageIter = pageImages.keySet().iterator();
//		while (imageIter.hasNext()) {
//			String key = (String) imageIter.next();
//			PDXObjectImage image = (PDXObjectImage) pageImages.get(key);
//			image.write2file(getFileNameWithOutExtention(f) + "/" + "image_" + i);
//			i ++;
//		}
//	}
//}
//document.close();
//}