package org.jakz.common;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.event.ListSelectionEvent;

import org.apache.logging.log4j.Logger;

/**
 * Generic application unit for performing parallel work.
 * @author johan
 */
public class ParallelWorker implements Runnable, Callable<List<Workable>>, Serializable
{
	public enum WorkerState {IDLE,STARTED,EMPTY,NOTDONE,DONE};
	private Logger log;
	
	private class MasterInfo
	{
		public InetAddress address;
		public Integer port;
		
	}
	
	private static final long serialVersionUID = -8206757731897018429L;
	private ReentrantReadWriteLock lock;
	private WorkerState workerState;
	private SharedStore<String, Object> sharedStore;
	//private Thread latestCallThread;
	public final String sessionID;
	public final int childID;
	public final String globalDescriptor;
	private int childIDTemplateCursor;
	private MasterInfo masterInfo;
	public String integritySecret;
	private boolean setting_exitWhenDone;
	private boolean runSwitch;
	private boolean remote;
	
	private ExecutorService executor;
	private List<Workable> workQueue;
	private Workable currentWork;
	private List<Future<List<Workable>>> futureFinishedWork;
	private List<Workable> finishedWork;
	//private Exception lastCallException;
	
	private HashMap<Integer, ParallelWorker> child;
	private ParallelWorker parent;
	
	private Integer localParallelism;
	
	private void init()
	{
		lock.writeLock().lock();
			workerState=WorkerState.IDLE;
			sharedStore=new SharedStore<String,Object>();
			masterInfo=new MasterInfo();
			childIDTemplateCursor=0; //Integer.MIN_VALUE;
			workQueue=new LinkedList<Workable>();
			currentWork=null;
			futureFinishedWork=new ArrayList<Future<List<Workable>>>();
			finishedWork=new LinkedList<Workable>();
			child = new HashMap<Integer, ParallelWorker>();
			executor=Executors.newCachedThreadPool();
			parent=null;
			localParallelism=4; //default setting
			remote=false;
			integritySecret="****aosdifjaosdij!!!!!!apsdfjio1234123****";
			setting_exitWhenDone=false;
			runSwitch=true;
		lock.writeLock().unlock();
	}
	
	public ParallelWorker(String nSessionID, Logger nLog) 
	{
		lock = new ReentrantReadWriteLock();
		init();
		lock.writeLock().lock();
			sessionID=nSessionID;
			childID=childIDTemplateCursor;
			log=nLog;
			globalDescriptor="ParallelWorker:"+sessionID+":"+childID;
		lock.writeLock().unlock();
		
	}
	
	private ParallelWorker(String nSessionID, int nChildID, Logger nLog) 
	{
		lock = new ReentrantReadWriteLock();
		init();
		lock.writeLock().lock();
			sessionID=nSessionID;
			childID=nChildID;
			childIDTemplateCursor=childID;
			log=nLog;
			globalDescriptor="ParallelWorker:"+sessionID+":"+childID+":";
		lock.writeLock().unlock();
		
	}
	
	/**
	 * Closes and cleans up resources
	 */
	public void close()
	{
		executor.shutdown();
	}
	
	/**
	 * Sets the {@link ExecutorService} for the object to use.
	 * @param nExecutor new {@link ExecutorService}
	 * @return this
	 */
	public ParallelWorker setExecutor(ExecutorService nExecutor)
	{
		lock.writeLock().lock();
			executor=nExecutor;
		lock.writeLock().unlock();
		return this;
	}
	
	public ParallelWorker setSharedStore(SharedStore<String,Object> nSharedStore)
	{
		lock.writeLock().lock();
			sharedStore=nSharedStore;
		lock.writeLock().unlock();
		return this;
	}
	
	public ParallelWorker setParent(ParallelWorker nParent)
	{
		lock.writeLock().lock();
			parent=nParent;
		lock.writeLock().unlock();
		return this;
	}
	
	/**
	 * Sets the parallelism
	 * @param nParallelism
	 * @return this
	 */
	public ParallelWorker setParallelism(Integer nParallelism)
	{
		lock.writeLock().lock();
			localParallelism=nParallelism;
		lock.writeLock().unlock();
		return this;
	}
	
	public int getParallelism()
	{
		lock.readLock().lock();
		int toreturn = localParallelism;
		lock.readLock().unlock();
		return toreturn;
	}
	
	public ParallelWorker setRemote(boolean nRemote)
	{
		lock.writeLock().lock();
			remote=nRemote;
		lock.writeLock().unlock();
		return this;
	}
	
	public boolean getRemote()
	{
		boolean toreturn;
		lock.readLock().lock();
			toreturn=remote;
		lock.readLock().unlock();
		return toreturn;
	}
	
	public ParallelWorker setIntegritySecret(String nIntegritySecret)
	{
		lock.writeLock().lock();
			integritySecret=nIntegritySecret;
		lock.writeLock().unlock();
		
		return this;
	}
	
	/**
	 * Gets the number of {@link ParallelWorker}s under this object's control. 
	 * @return the number
	 */
	public int getNumChildren()
	{
		lock.readLock().lock();
			int toreturn = child.size();
		lock.readLock().unlock();
		return toreturn;
	}
	
	private int claimNewChildID() throws ParallelWorkerException
	{
		lock.readLock().lock();
		
		if(childIDTemplateCursor==Integer.MAX_VALUE)
		{
			lock.readLock().unlock();
			throw new ParallelWorkerException("Can't create new worker id - workerIdIndex is at maximum.");
		}
		else
		{
			lock.writeLock().lock();
			int toreturn =++childIDTemplateCursor;
			lock.writeLock().unlock();
			lock.readLock().unlock();
			return toreturn;
		}
	}
	
	/**
	 * Spawns a new {@link ParallelWorker} based on this object. The id of the new object is set according to this object's id template cursor. The newly created {@link ParallelWorker} uses the same {@link ExecutorService} as this object.
	 * @return THE NEW {@link ParallelWorker}
	 * @throws ParallelWorkerException 
	 */
	public ParallelWorker spawnLocalChild() throws ParallelWorkerException
	{
		int newId=claimNewChildID();
		ParallelWorker nw = new ParallelWorker(sessionID,newId,log).setExecutor(executor).setSharedStore(sharedStore);
		putChild(nw);
		nw.submit();
		return nw;
	}
	
	private ParallelWorker putChild(ParallelWorker newChild) throws ParallelWorkerException
	{
		if(newChild.sessionID!=sessionID)
			throw new ParallelWorkerException("Child session ID not compatible with parent session ID");
		
		lock.writeLock().lock();
		if(!child.containsKey(newChild.childID)&&newChild.childID<=Integer.MAX_VALUE)
		{
			newChild.setParent(this);
			child.put(newChild.childID, newChild);
		}
		else
		{
			lock.writeLock().unlock();
			throw new ParallelWorkerException("Duplicate ParallelWorker id in children tree. Id:"+newChild.childID);
		}
		lock.writeLock().unlock();
		return this;
	}
	
	/**
	 * Submits this {@link ParallelWorker} to work in it's own thread using the set {@link ExecutorService}.
	 */
	public ParallelWorker submit()
	{	
		lock.writeLock().lock();
			runSwitch=true;
			//futureFinishedWork = executor.submit((Callable<List<Workable>>)this);
			Future<List<Workable>> res = executor.submit((Callable<List<Workable>>)this);
			//futureFinishedWork.add(res);
		log.debug(globalDescriptor+" submit issued");
		lock.writeLock().unlock();
		return this;
	}
	
	
	public ParallelWorker stop() throws WorkableFailureException
	{
		lock.writeLock().lock();
			runSwitch=false;
		lock.writeLock().unlock();
		
		lock.readLock().lock();
		if(currentWork!=null)
			currentWork.stop();
		Iterator<Workable> it = workQueue.iterator();
		while(it.hasNext())
		{
			Workable c = it.next();
			c.stop();
		}
		lock.readLock().unlock();
		log.info(globalDescriptor+" stop issued");
		
		return this;
	}
	
	public boolean getRun()
	{
		boolean toreturn = true;
		lock.readLock().lock();
			toreturn=runSwitch;
		lock.readLock().unlock();
		return toreturn;
	}
	
	public boolean isDone()
	{
		boolean toreturn = true;
		lock.readLock().lock();
		if(workerState==WorkerState.DONE||workerState==WorkerState.EMPTY)
		{
			toreturn=true;
		}
		lock.readLock().unlock();
		
		log.info(globalDescriptor+" isDone:"+toreturn);
		
		return toreturn;
	}
	
	public boolean isDoneRec()
	{
		boolean toreturn = true;
		lock.readLock().lock();
		if(child.size()>0)
		{
			Integer[] keys = new Integer[child.keySet().size()];
			keys = child.keySet().toArray(keys);
			for(int i=0; i<keys.length; i++)
			{
				ParallelWorker c = child.get(keys[i]);
				if(!c.isDoneRec())
				{
					lock.readLock().unlock();
					return false;
				}
			}
		}
		
		if(workerState!=WorkerState.DONE&&workerState!=WorkerState.EMPTY)
		{
			toreturn=false;
		}

		lock.readLock().unlock();
		
		log.info(globalDescriptor+" isDoneRec:"+toreturn);
		
		return toreturn;
	}
	
	public ParallelWorker waitForDone(long timeoutMillis) throws InterruptedException, TimeoutException
	{
		int localtimeoutmillis = 500;
		log.info(globalDescriptor+" waiting for done");
		int i=0;
		
		for(;!isDoneRec()&&(i*localtimeoutmillis<timeoutMillis||timeoutMillis<0); i++)
		{
			Thread.sleep(localtimeoutmillis);
			/*
			synchronized(this)
			{
				wait(localtimeoutmillis);
			}
			*/
		}
		
		/*
		if(parent!=null)
		{
			synchronized(parent)
			{
				notifyAll();
			}
		}
		
		synchronized(this)
		{
			notifyAll();
		}
		*/
		
		
		log.info(globalDescriptor+" is done waiting with i="+i+" and localtimeoutmillis="+localtimeoutmillis);
		
		if(i*localtimeoutmillis>=timeoutMillis)
		{
			log.error(globalDescriptor+" waited more than specified timeout (ms) "+timeoutMillis);
			throw new TimeoutException("Waited more than specified timeout (ms) "+timeoutMillis);
		}
		
		return this;
	}
	
	
	
	public WorkerState getWorkerState()
	{
		WorkerState toreturn;
		lock.readLock().lock();
			toreturn = workerState;
		lock.readLock().unlock();
		return toreturn;
		
	}
	
	/**
	 * Gets the work assigned to this {@link ParallelWorker}. <br><strong>Warning: Manipulating or reading internal parts can have unforeseen consequences when running threaded work. Make sure to lock the object you are using.</strong>
	 * @return the work
	 */
	public List<Workable> getWork()
	{
		return workQueue;
	}
	
	public List<Future<List<Workable>>> getFutureFinishedWork()
	{
		return futureFinishedWork;
	}
	
	public List<Workable> getFinishedWork()
	{
		return finishedWork;
	}
	
	/**
	 * Assigns work to this {@link ParallelWorker}.
	 * @param ntask
	 * @return
	 */
	public ParallelWorker setWork(List<Workable> ntask)
	{
		lock.writeLock().lock();
			workQueue=ntask;
		lock.writeLock().unlock();
		log.debug(globalDescriptor+" work set");
		return this;
	}
	
	/**
	 * Adds work to this {@link ParallelWorker}.
	 * @param ntask
	 * @return
	 */
	public ParallelWorker addWork(Workable ntask)
	{
		lock.writeLock().lock();
			workQueue.add(ntask);
		lock.writeLock().unlock();
		log.debug(globalDescriptor+" work added");
		return this;
	}
	
	public Workable popWork()
	{
		Workable toReturn = null;
		lock.writeLock().lock();
			if(workQueue.size()>0)
				toReturn = workQueue.remove(0);
		lock.writeLock().unlock();
		log.debug(globalDescriptor+" work popped");
		return toReturn;
	}
	
	private void sortWork() throws WorkableFailureException
	{
		lock.writeLock().lock();
			if(workQueue.size()>0)
			{
				workQueue.sort(workQueue.get(0).getComparator());
			}
		lock.writeLock().unlock();
		//log.debug(globalDescriptor+" sorted work");
	}
	
	private void divideWork() throws WorkableFailureException
	{
		
		List<Workable> newWorkQueue = new ArrayList<Workable>();
		lock.writeLock().lock();
		int parallelism = child.size()-1;
		int iWorkable =0;
		for(;iWorkable<workQueue.size()&&workQueue.size()+newWorkQueue.size()<parallelism;iWorkable++)
		{
			Workable cWorkable = workQueue.get(iWorkable);
			if(cWorkable.canDivide())
			{
				List<Workable> dividedWork;
				//if(localParallelism==null)
					//dividedWork = cWorkable.divideMax();
				//else
					dividedWork = cWorkable.divide(parallelism-(workQueue.size()+newWorkQueue.size())-(workQueue.size()-1-iWorkable)+1);
				
				newWorkQueue.addAll(dividedWork);
			}
			else newWorkQueue.add(cWorkable);
		}
		
		if(iWorkable<workQueue.size())
			newWorkQueue.addAll(workQueue.subList(iWorkable, workQueue.size()));
		workQueue=newWorkQueue;
		log.debug(globalDescriptor+" divided work until "+workQueue.size());
		lock.writeLock().unlock();
	}
	
	private void spawnLocalChildren() throws ParallelWorkerException
	{
		lock.writeLock().lock();
		int i=0;
		for(; i<localParallelism-1; i++)
		{
			spawnLocalChild();
		}
		lock.writeLock().unlock();
		log.debug(globalDescriptor+" spawned "+i+" child(ren)");
	}
	
	private void administerWork() throws ParallelWorkerException, InterruptedException, WorkableFailureException
	{
		ParallelWorker currentParent;
		int current_localParallelism;
		int workQueueSize;
		int childSize;
		boolean runSwitchStatus;
		boolean current_setting_exitWhenDone;
		
		sortWork();
		
		while(true)
		{
			lock.readLock().lock();
				current_setting_exitWhenDone = setting_exitWhenDone;
				current_localParallelism=localParallelism;
				workQueueSize=workQueue.size();
				childSize=child.size();
			lock.readLock().unlock();
			
			if(childSize>0&&workQueueSize>0&&workQueueSize<current_localParallelism)
				divideWork();
			
			
			lock.readLock().lock();
				current_localParallelism=localParallelism;
				currentParent = parent;
				workQueueSize=workQueue.size();
				childSize=child.size();
			lock.readLock().unlock();
			
			//master
			if(currentParent==null&&childSize+1<current_localParallelism)
			{
				spawnLocalChildren();
			}
			
			//all workers
			if(workQueueSize>0)
			{
				lock.writeLock().lock();
				currentWork = popWork();
				if(currentWork!=null)
					log.debug(globalDescriptor+" popped work queue");
				lock.writeLock().unlock();
			}
			else if(currentParent!=null)
			{
				lock.writeLock().lock();
					currentWork = currentParent.popWork();
					if(currentWork!=null)
						log.debug(globalDescriptor+" popped parent work queue");
				lock.writeLock().unlock();
			}
					
			if(currentWork!=null)
			{
				lock.writeLock().lock();
					workerState=WorkerState.STARTED;
				lock.writeLock().unlock();
				log.debug(globalDescriptor+" performing popped work");
				
				try
				{
					currentWork.performWork();	//Here is where the work is done!
					lock.writeLock().lock();
						finishedWork.add(currentWork);
						currentWork=null;
						workerState=WorkerState.DONE;
						if(parent!=null)
						{
							//throws java.lang.IllegalMonitorStateException for some reason
							/*{
							synchronized(parent)
							{
								notifyAll();
							}
							*/
						}
					lock.writeLock().unlock();
					log.debug(globalDescriptor+" performed work");
				}
				catch (WorkableFailureException e)
				{
					lock.writeLock().lock();
						finishedWork.add(currentWork);
						currentWork=null;
						workerState=WorkerState.NOTDONE;
					lock.writeLock().unlock();
					log.error(globalDescriptor+" performed work NOT done, reason: "+e.toString());
				}
				
				
			}
			else if(getWorkerState()!=WorkerState.NOTDONE)
			{
				lock.writeLock().lock();
					workerState=WorkerState.EMPTY;
				lock.writeLock().unlock();
			}
			
			
			
			lock.readLock().lock();
				runSwitchStatus=runSwitch;
			lock.readLock().unlock();
			
			//parent stopped
			//log.debug(globalDescriptor+" runSwitchStatus:"+runSwitchStatus+" parent:"+parent);
			//if(parent!=null)
				//log.debug(globalDescriptor+" parent getRun:"+parent.getRun());
			if(runSwitchStatus&&parent!=null&&!parent.getRun())
			{
				//self stop
				log.debug(globalDescriptor+" stopping self because of parent stop");
				stop();
				lock.readLock().lock();
					runSwitchStatus=runSwitch;
				lock.readLock().unlock();
			}
			
			if(current_setting_exitWhenDone || !runSwitchStatus)
			{
				log.debug(globalDescriptor+" breaking work loop");
				break;
			}
			
			if(getWorkerState()==WorkerState.EMPTY)
			{
				log.debug(globalDescriptor+" empty and sleeping");
				Thread.sleep(50);
			}
			
		}
	}
	
	/**
	 * Runs this {@link ParallelWorker} according to the implemented {@link Runnable} interface.
	 */
	@Override
	public void run()
	{
		call();
	}
	
	/**
	 * Calls this {@link ParallelWorker} according to the implemented {@link Callable} interface.
	 * @throws WorkableFailureException 
	 * @throws ParallelWorkerException 
	 */
	@Override
	public List<Workable> call()
	{
		log.info(globalDescriptor+" initiating call");
		try
		{
			administerWork();
		}
		catch (Exception e) 
		{
			lock.writeLock().lock();
				//lastCallException=e;
				//e.printStackTrace();
				workerState=WorkerState.NOTDONE;
			lock.writeLock().unlock();
			log.error(globalDescriptor+" worker state NOT done, reason: "+e.toString()+"\n"+Util.getStackTraceString(e));
		}
		finally
		{
			//notifyAll();
			if(lock.writeLock().isHeldByCurrentThread())
				lock.writeLock().unlock();
			
			//lock.readLock().unlock();
			
		}
		log.info(globalDescriptor+" ending call");
		return workQueue;
	}

	
}
