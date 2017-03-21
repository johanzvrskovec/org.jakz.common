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
import java.util.Enumeration;
import java.util.Iterator;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
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

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileProcessor 
{
	public static final int BUFFER_SIZE = 16384;
	
	public static void unzipFile(String inputFilePath, String outputFilePath) throws IOException
	{
		ZipFile zipFile = new ZipFile(inputFilePath);
		try 
		{
		  Enumeration<? extends ZipEntry> entries = zipFile.entries();
		  while (entries.hasMoreElements()) 
		  {
		    ZipEntry entry = entries.nextElement();
		    File entryDestination = new File(outputFilePath,  entry.getName());
		    if (entry.isDirectory()) 
		    {
		        entryDestination.mkdirs();
		    } else 
		    {
		        entryDestination.getParentFile().mkdirs();
		        InputStream in = zipFile.getInputStream(entry);
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
		  zipFile.close();
		}
	}
	
	public static void decryptFile
	(
	        String inputFilePath,
	        String keyFilePath,
	        char[] passwd,
	        String outputFilePath)
	        throws IOException, NoSuchProviderException
	    {
	        InputStream in = new BufferedInputStream(new FileInputStream(inputFilePath));
	        InputStream keyIn = new BufferedInputStream(new FileInputStream(keyFilePath));
	        OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFilePath));
	        decryptFile(in, keyIn, passwd, out);
	        keyIn.close();
	        in.close();
	        out.close();
	    }

	    /**
	     * decrypt the passed in message stream
	     */
	    public static void decryptFile
	    (
	        InputStream in,
	        InputStream keyIn,
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
	                PGPUtil.getDecoderStream(keyIn));

	            while (sKey == null && it.hasNext())
	            {
	                pbe = (PGPPublicKeyEncryptedData)it.next();
	                
	                sKey = findSecretKey(pgpSec, pbe.getKeyID(), passwd);
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
	    static PGPPrivateKey findSecretKey(PGPSecretKeyRingCollection pgpSec, long keyID, char[] pass)
	        throws PGPException, NoSuchProviderException
	    {
	        PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);

	        if (pgpSecKey == null)
	        {
	            return null;
	        }

	        return pgpSecKey.extractPrivateKey(pass, "BC");
	    }
	    
	    static PGPPublicKey readPublicKey(String fileName) throws IOException, PGPException
	    {
	        InputStream keyIn = new BufferedInputStream(new FileInputStream(fileName));
	        PGPPublicKey pubKey = readPublicKey(keyIn);
	        keyIn.close();
	        return pubKey;
	    }

	    /**
	     * A simple routine that opens a key ring file and loads the first available key
	     * suitable for encryption.
	     * 
	     * @param input
	     * @return
	     * @throws IOException
	     * @throws PGPException
	     */
	    static PGPPublicKey readPublicKey(InputStream input) throws IOException, PGPException
	    {
	        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(
	            PGPUtil.getDecoderStream(input));

	        //
	        // we just loop through the collection till we find a key suitable for encryption, in the real
	        // world you would probably want to be a bit smarter about this.
	        //

	        Iterator keyRingIter = pgpPub.getKeyRings();
	        while (keyRingIter.hasNext())
	        {
	            PGPPublicKeyRing keyRing = (PGPPublicKeyRing)keyRingIter.next();

	            Iterator keyIter = keyRing.getPublicKeys();
	            while (keyIter.hasNext())
	            {
	                PGPPublicKey key = (PGPPublicKey)keyIter.next();

	                if (key.isEncryptionKey())
	                {
	                    return key;
	                }
	            }
	        }

	        throw new IllegalArgumentException("Can't find encryption key in key ring.");
	    }

	    static PGPSecretKey readSecretKey(String fileName) throws IOException, PGPException
	    {
	        InputStream keyIn = new BufferedInputStream(new FileInputStream(fileName));
	        PGPSecretKey secKey = readSecretKey(keyIn);
	        keyIn.close();
	        return secKey;
	    }

	    /**
	     * A simple routine that opens a key ring file and loads the first available key
	     * suitable for signature generation.
	     * 
	     * @param input stream to read the secret key ring collection from.
	     * @return a secret key.
	     * @throws IOException on a problem with using the input stream.
	     * @throws PGPException if there is an issue parsing the input stream.
	     */
	    static PGPSecretKey readSecretKey(InputStream input) throws IOException, PGPException
	    {
	        PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(
	            PGPUtil.getDecoderStream(input));

	        //
	        // we just loop through the collection till we find a key suitable for encryption, in the real
	        // world you would probably want to be a bit smarter about this.
	        //

	        Iterator keyRingIter = pgpSec.getKeyRings();
	        while (keyRingIter.hasNext())
	        {
	            PGPSecretKeyRing keyRing = (PGPSecretKeyRing)keyRingIter.next();

	            Iterator keyIter = keyRing.getSecretKeys();
	            while (keyIter.hasNext())
	            {
	                PGPSecretKey key = (PGPSecretKey)keyIter.next();

	                if (key.isSigningKey())
	                {
	                    return key;
	                }
	            }
	        }

	        throw new IllegalArgumentException("Can't find signing key in key ring.");
	    }
	    
	    static byte[] compressFile(String fileName, int algorithm) throws IOException
	    {
	        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
	        PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(algorithm);
	        PGPUtil.writeFileToLiteralData(comData.open(bOut), PGPLiteralData.BINARY,
	            new File(fileName));
	        comData.close();
	        return bOut.toByteArray();
	    }

}
