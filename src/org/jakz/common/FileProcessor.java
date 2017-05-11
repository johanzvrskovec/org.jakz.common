package org.jakz.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.util.io.Streams;
import org.jakz.common.util.FileUtil;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Class to process files: encryption, compression et.c.
 * To use BouncyCastle encryption methods, you will have to {@code Security.addProvider(new BouncyCastleProvider());}
 * @author johkal
 *
 */
public class FileProcessor 
{
	public static final int BUFFER_SIZE = 16384;
	
	protected HashSet<String> temporaryFileSafeDeleteCollection = new HashSet<String>();
	
	/**
	 * Must be used on a non-existing File to be able to use {@link FileProcessor.safeDeleteTemporaryFile} on it.
	 * @param toBeDeleted
	 * @throws IOException
	 */
	public void markTemporaryFileForSafeDeletion(File toBeDeleted) throws IOException
	{
		boolean safe = false;
		if(toBeDeleted.exists())
		{
			if(toBeDeleted.isDirectory())
			{
				safe = toBeDeleted.list().length==0;
			}
		}
		else
			safe=true;
		
		if(!safe)
			throw new IOException("This file or folder is not safe for deletion, because it is not empty or non existant.");
		
		temporaryFileSafeDeleteCollection.add(toBeDeleted.getAbsolutePath());
	}
	
	/**
	 * Deletes a file in a safe way to avoid unintentional deletion of existing files and folders. 
	 * @param toBeDeleted
	 * @param fallbackToDeleteOnExit
	 * @return true if the file was determined to not exist anymore or if the file was determined to be safe according to the use of {@link FileProcessor.markTemporaryFileForSafeDeletion} and a successful delete operation on it was performed. false otherwise.
	 * @throws IOException
	 */
	public boolean safeDeleteTemporaryFile(File toBeDeleted, boolean fallbackToDeleteOnExit) throws IOException
	{
		if(!toBeDeleted.exists())
			return true;
		
		boolean safe=false;
		String toBeDeletedPath = toBeDeleted.getAbsolutePath();
		safe = temporaryFileSafeDeleteCollection.contains(toBeDeletedPath);
		if(safe)
		{
			temporaryFileSafeDeleteCollection.remove(toBeDeletedPath);
			if(toBeDeleted.isFile())
				return FileUtil.deleteFileIfExistsOldCompatSafe(toBeDeleted,fallbackToDeleteOnExit);
			else if(toBeDeleted.isDirectory())
				return FileUtil.deleteDirectoryIfExistsOldCompatSafe(toBeDeleted,true,fallbackToDeleteOnExit);
			else
				throw new IOException("File to be deleted is of unknown type.");
		}
		else
			return false;
	}
	
	/**
	 * Unzips a file to a destination directory.
	 * @param inputFile
	 * @param outputDir
	 * @throws IOException
	 */
	public static void unzipFile(ZipFile inputFile, File outputDir, boolean overwrite) throws IOException
	{
		unzipFile(inputFile,outputDir,overwrite,null);
	}
	
	/**
	 * Unzips a file to a destination directory. If zipEntries is passed, will contain the processed entries.
	 * @param inputFile
	 * @param outputDir
	 * @param zipEntries Will be filled with the processed entries
	 * @throws IOException
	 */
	public static void unzipFile(ZipFile inputFile, File outputDir, boolean overwrite, HashMap<String,ZipEntry> zipEntries) throws IOException
	{
		try 
		{
		  Enumeration<? extends ZipEntry> entries = inputFile.entries();
		  while (entries.hasMoreElements()) 
		  {
		    ZipEntry entry = entries.nextElement();
		    if(zipEntries!=null)
		    	zipEntries.put(entry.getName(), entry);
		    File entryDestination = new File(outputDir,  entry.getName());
		    if (entry.isDirectory()) 
		    {
		        entryDestination.mkdirs();
		    } else 
		    {
		    	if(entryDestination.exists()&&!overwrite)
		    		throw new IOException("File "+entry.getName()+" exists already in the target location.");
		        entryDestination.getParentFile().mkdirs();
		        InputStream in = inputFile.getInputStream(entry);
		        OutputStream out = new FileOutputStream(entryDestination);
		        
		        byte[] bytesIn = new byte[BUFFER_SIZE];
		        int read = 0;
		        
		        while ((read = in.read(bytesIn)) != -1) 
		        {
		            out.write(bytesIn, 0, read);
		        }
		        out.close();
		    }
		  }
		} 
		finally 
		{
			inputFile.close();
		}
	}
	
	public static void PGPDecryptFile
	(
	        File inputFile,
	        File secretKeyFile,
	        char[] passwd,
	        File outputFile)
	        throws IOException, NoSuchProviderException
	    {
	        InputStream in = new BufferedInputStream(new FileInputStream(inputFile));
	        InputStream keyIn = new BufferedInputStream(new FileInputStream(secretKeyFile));
	        OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
	        PGPDecryptFile(in, keyIn, passwd, out);
	        keyIn.close();
	        in.close();
	        out.close();
	    }

	    /**
	     * decrypt the passed in message stream
	     */
	    public static void PGPDecryptFile
	    (
	        InputStream in,
	        InputStream secretKeyIn,
	        char[]      passwd,
	        OutputStream out)
	        throws IOException, NoSuchProviderException
	    {
	        in = PGPUtil.getDecoderStream(in);
	        
	        try
	        {
	            PGPObjectFactory pgpF = new PGPObjectFactory(in);
	            PGPEncryptedDataList    enc;

	            Object                  o = pgpF.nextObject();
	            //
	            // the first object might be a PGP marker packet.
	            //
	            if (o instanceof PGPEncryptedDataList)
	            {
	                enc = (PGPEncryptedDataList)o;
	            }
	            else
	            {
	                enc = (PGPEncryptedDataList)pgpF.nextObject();
	            }
	            
	            //
	            // find the secret key
	            //
	            Iterator                    it = enc.getEncryptedDataObjects();
	            PGPPrivateKey               sKey = null;
	            PGPPublicKeyEncryptedData   pbe = null;
	            PGPSecretKeyRingCollection  pgpSec = new PGPSecretKeyRingCollection(
	                PGPUtil.getDecoderStream(secretKeyIn));

	            while (sKey == null && it.hasNext())
	            {
	                pbe = (PGPPublicKeyEncryptedData)it.next();
	                
	                sKey = PGPFindSecretKey(pgpSec, pbe.getKeyID(), passwd);
	            }
	            
	            if (sKey == null)
	            {
	                throw new IllegalArgumentException("secret key for message not found.");
	            }
	    
	            InputStream         clear = pbe.getDataStream(sKey, "BC");
	            
	            PGPObjectFactory    plainFact = new PGPObjectFactory(clear);
	            
	            Object              message = plainFact.nextObject();
	    
	            if (message instanceof PGPCompressedData)
	            {
	                PGPCompressedData   cData = (PGPCompressedData)message;
	                PGPObjectFactory    pgpFact = new PGPObjectFactory(cData.getDataStream());
	                
	                message = pgpFact.nextObject();
	            }
	            
	            if (message instanceof PGPLiteralData)
	            {
	                PGPLiteralData ld = (PGPLiteralData)message;
	                InputStream unc = ld.getInputStream();
	                Streams.pipeAll(unc, out);
	            }
	            else if (message instanceof PGPOnePassSignatureList)
	            {
	                throw new PGPException("encrypted message contains a signed message - not literal data.");
	            }
	            else
	            {
	                throw new PGPException("message is not a simple encrypted file - type unknown.");
	            }

	            if (pbe.isIntegrityProtected())
	            {
	                if (!pbe.verify())
	                {
	                    System.err.println("message failed integrity check");
	                }
	                else
	                {
	                    System.err.println("message integrity check passed");
	                }
	            }
	            else
	            {
	                System.err.println("no message integrity check");
	            }
	        }
	        catch (PGPException e)
	        {
	            System.err.println(e);
	            if (e.getUnderlyingException() != null)
	            {
	                e.getUnderlyingException().printStackTrace();
	            }
	        }
	    }

	    /**
	     * Search a secret key ring collection for a secret key corresponding to keyID if it
	     * exists.
	     * 
	     * @param pgpSec a secret key ring collection.
	     * @param keyID keyID we want.
	     * @param pass passphrase to decrypt secret key with.
	     * @return
	     * @throws PGPException
	     * @throws NoSuchProviderException
	     */
	    static PGPPrivateKey PGPFindSecretKey(PGPSecretKeyRingCollection pgpSec, long keyID, char[] pass)
	        throws PGPException, NoSuchProviderException
	    {
	        PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);

	        if (pgpSecKey == null)
	        {
	            return null;
	        }

	        return pgpSecKey.extractPrivateKey(pass, "BC");
	    }

}
