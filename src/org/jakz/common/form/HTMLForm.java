package org.jakz.common.form;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.text.StringEscapeUtils;
import org.jakz.common.JSONObject;
import org.jakz.common.TypedValue;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

public class HTMLForm extends Form
{
	
	private Element workingElement;
	private HTMLForm masterHTMLForm;
	
	
	public boolean settingGenerateViewLinks;
	public String settingHTMLFormSrc,settingHTMLFormArgumentNameId,settingHTMLFormArgumentFunction;
	//public ArrayList<String> settingViewLinkKeys;
	
	protected void init()
	{
		workingElement=null;
		masterHTMLForm=this;
		settingGenerateViewLinks=true;
		settingHTMLFormSrc ="";
		settingHTMLFormArgumentNameId="HTMLFormArgument";
		//settingViewLinkKeys=new ArrayList<String>();
		settingHTMLFormArgumentFunction="HTMLFormSubmit";
	}
	
	public HTMLForm(String nid, FieldType ntype)
	{
		super(nid,ntype);
		//workingElement = new Element(Tag.valueOf("div"), "");
		init();
	}
	
	/**
	 * Crates a HTMLForm populated with a <strong>shallow</strong> copy of a Form object 
	 * @param nformTemplate
	 */
	public HTMLForm(Form nformTemplate)
	{
		super(nformTemplate.id,nformTemplate.type);
		init();
		scopy(nformTemplate);
	}
	
	public HTMLForm setWorkingElement(Element nWorkingElement)
	{
		workingElement=nWorkingElement;
		return this;
	}
	
	public HTMLForm setMasterHTMLForm(HTMLForm nMasterHTMLForm)
	{
		masterHTMLForm=nMasterHTMLForm;
		return this;
	}
	
	public HTMLForm getMasterHTMLForm()
	{
		return masterHTMLForm;
	}
	
	public String getHTMLGlobalID()
	{
		return StringEscapeUtils.escapeHtml3(super.getHTMLGlobalID());
	}
	
	public String toString()
	{
		return workingElement.toString();
	}
	
	public Element getWorkingElement()
	{
		return workingElement;
	}
	
	public HTMLForm initWorkingElement()
	{
		if(workingElement==null)
			workingElement = new Element(Tag.valueOf("div"),"").attr("name", id);
		
		return this;
	}
	
	public HTMLForm generateHTMLFormJSHTML() throws FormException
	{
		if(workingElement!=null)
			workingElement.appendChild(getHTMLFormJSHTML());
		else
			workingElement=getHTMLFormJSHTML();
		return this;
	}
	
	public HTMLForm generateHTMLFormHTML() throws FormException
	{
		if(workingElement!=null)
			workingElement.appendChild(getHTMLFormHTML());
		else
			workingElement=getHTMLFormHTML();
		return this;
	}
	
	public HTMLForm generateHTMLForm() throws FormException
	{
		if(workingElement!=null)
			workingElement.appendChild(getFormHTML());
		else
			workingElement=getFormHTML();
		return this;
	}
	
	public HTMLForm generateHTMLView() throws FormException
	{
		
		if(workingElement!=null)
			workingElement.appendChild(getViewHTML());
		else
			workingElement=getViewHTML();
		return this;
	}
	
	
	public Element getHTMLFormJSHTML()
	{
		return HTMLForm.getHTMLFormJSHTML(masterHTMLForm);
	}
	
	protected static Element getHTMLFormJSHTML(HTMLForm masterForm)
	{
		Element script = new Element(Tag.valueOf("script"),"").attr("type", "text/javascript");
		
		String html = "";
		
		html+="function HTMLFormSubmit(var arg)";
		html+="{";
		html+="alert('KLICK!');";
		//html+="window.location = '"+settingViewLinkSrc+"?"+settingViewLinkKeyArgumentNameId+"='+arg;";
		html=html+"var formE = document.getElementById('"+masterForm.id+"');";
		html=html+"var inputE = document.getElementById('"+masterForm.settingHTMLFormArgumentNameId+"');";
		html+="inputE.value=arg;";
		html+="formE.submit();";
		html+="}";
		
		
		script.html(html);
		
		return script;
	}
	
	public Element getHTMLFormHTML()
	{
		return HTMLForm.getHTMLFormHTML(masterHTMLForm);
	}
	
	protected static Element getHTMLFormHTML(HTMLForm masterForm)
	{
		Element formE = new Element(Tag.valueOf("form"),"").attr("action",masterForm.settingHTMLFormSrc).attr("method", "post").attr("id", masterForm.id);
		
		formE.appendElement("input").attr("type", "hidden").attr("name", masterForm.settingHTMLFormArgumentNameId).attr("id", masterForm.settingHTMLFormArgumentNameId);
		
		return formE;
	}
	
	public Element getFormHTML() throws FormException
	{
		return HTMLForm.getFormHTML(this, masterHTMLForm);
	}
	
	//TODO
	protected static Element getFormHTML(HTMLForm source, HTMLForm masterForm) throws FormException
	{
		Element formElementToReturn;
		if(source.type==Form.FieldType.QRY)
		{
			formElementToReturn = new Element(Tag.valueOf("fieldset"),"");
			formElementToReturn.attr("name", source.id).attr("id",source.getHTMLGlobalID()).attr("form",masterForm.id);
			//toreturn.appendElement("legend").html(source.name).attr("name","name");
			formElementToReturn.appendElement("div").html(source.name).attr("name","name");
			formElementToReturn.appendElement("div").html(source.text).attr("name","text");
			
			for(int coli=0; coli<source.content.size(); coli++)
			{
				
				Form columnForm = source.content.getValueAt(coli);
				if(!columnForm.writeable)
					continue;
				
				Element valcontainer = formElementToReturn.appendElement("div").attr("name","valcontainer");
				
				
				
				
				valcontainer.appendElement("span").html(columnForm.name);
				
				TypedValue tv = columnForm.value;
				int type = tv.getType();
				
				Element inputElement;
				
				if(type==java.sql.Types.INTEGER)
					inputElement = valcontainer.appendElement("input").attr("type", "number");
				else if(type==java.sql.Types.DOUBLE)
					inputElement = valcontainer.appendElement("input").attr("type", "number");
				else if(type==java.sql.Types.BOOLEAN)
					inputElement = valcontainer.appendElement("input").attr("type", "checkbox");
				else if(type==java.sql.Types.VARCHAR||type==java.sql.Types.NVARCHAR||type==-16) //TODO SQL Server returns -16 for nvarchar(max)
					inputElement = valcontainer.appendElement("input").attr("type", "text");
				else if(type==java.sql.Types.TIMESTAMP)
					inputElement = valcontainer.appendElement("input").attr("type", "datetime");
				else if(type==java.sql.Types.BIGINT)
					inputElement = valcontainer.appendElement("input").attr("type", "number");
				else throw new FormException("Wrong value type for HTML generator. Type "+type+" form "+source.id);
				
				inputElement.attr("name",columnForm.id).attr("id",columnForm.getHTMLGlobalID()).attr("form",masterForm.id);
			}
			
		}
		/*
		else if(source.type==Form.FieldType.INFO)
		{
			formElementToReturn = new Element(Tag.valueOf("div"),"").attr("name", source.id).html(source.text);
		}
		*/
		else if(source.type==Form.FieldType.FRM)
		{
			//toreturn = new Element(Tag.valueOf("form"),"").attr("id", source.getGlobalID()).attr("name", source.getGlobalID());
			formElementToReturn = new Element(Tag.valueOf("div"),"").attr("name", source.id);

			
			for(int icontent=0; icontent<source.content.size(); icontent++)
			{
				Element contentElement = getFormHTML(new HTMLForm(source.content.getAt(icontent).value),masterForm);
				formElementToReturn.appendChild(contentElement);
			}
		}
		else throw new FormException("Unrecognizable Form type "+source.type);
		
		return formElementToReturn;
	}
	

	public Element getViewHTML() throws FormException
	{
		return HTMLForm.getViewHTML(this, masterHTMLForm);
	}
	
	protected static Element getViewHTML(HTMLForm source, HTMLForm masterForm) throws FormException
	{
		Element toreturn;
		
		if(source.type==FieldType.VAR)
		{
			toreturn =  new Element(Tag.valueOf("td"),"").attr("name", source.id).attr("id",source.id);
			TypedValue tv = source.value;
			int type = tv.getType();
			
			if(type==java.sql.Types.INTEGER)
			{
				toreturn.attr("type", "number");
				if(tv.getValueInteger()!=null)
					toreturn.html(""+tv.getValueInteger());
			}
			else if(type==java.sql.Types.DOUBLE)
			{
				toreturn.attr("type", "number");
				if(tv.getValueDouble()!=null)
					toreturn.html(""+tv.getValueDouble());
			}
			else if(type==java.sql.Types.BOOLEAN)
			{
				toreturn.attr("type", "checkbox");
				if(tv.getValueBoolean()!=null)
					toreturn.html(""+tv.getValueBoolean());
			}
			else if(type==java.sql.Types.VARCHAR||type==java.sql.Types.NVARCHAR||type==-16) //TODO SQL Server returns -16 for nvarchar(max)
			{
				toreturn.attr("type", "text");
				if(tv.getValueVarchar()!=null)
					toreturn.html(""+tv.getValueVarchar());
			}
			else if(type==java.sql.Types.TIMESTAMP)
			{
				toreturn.attr("type", "datetime");
				if(tv.getValueTimestamp()!=null)
				{
					Date d = new Date(tv.getValueTimestamp());
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
					toreturn.html(df.format(d));
				}
			}
			else if(type==java.sql.Types.BIGINT)
			{
				toreturn.attr("type", "number");
				if(tv.getValueBigint()!=null)
					toreturn.html(""+tv.getValueBigint());
			}
			else throw new FormException("Wrong value type for HTML generator. Type "+type+" form "+source.id+" column "+source.id);
			
		}
		else if(source.type==FieldType.QRY)
		{
			//if(masterForm.settingGenerateViewLinks)
			//{
				//toreturn=new Element(Tag.valueOf("a"),"").attr("href",masterForm.settingViewLinkSrc).attr("target", "_blank");
				//toreturn = toreturn.attr("name", source.getGlobalID()).attr("id",source.getGlobalID());
				//elementHead=toreturn.appendElement("tr").attr("onclick", "alert('klick');");
			//}
			
			
			toreturn =  new Element(Tag.valueOf("tr"),"").attr("name", source.id).attr("id",source.getHTMLGlobalID());
			if(masterForm.settingGenerateViewLinks)
			{
				try
				{
				toreturn.attr("onclick", "HTMLFormEditRow('"+URLEncoder.encode(source.toJSONObject().toString(),"UTF-8") +"');");
				}
				catch (UnsupportedEncodingException e)
				{
					throw new FormException("Unsupported encoding", e);
				}
			}
			
			for(int icontent=0; icontent<source.content.size(); icontent++)
			{
				HTMLForm currentForm = new HTMLForm(source.content.getAt(icontent).value);
				
				if(currentForm.type==FieldType.VAR)
				{
					Element contentElement = HTMLForm.getViewHTML(currentForm,masterForm);
					toreturn.appendChild(contentElement);
				}
				else throw new FormException("The content of a query Form must be a variable Form");
			}
		}
		else if(source.type==FieldType.FRM)
		{
			toreturn = new Element(Tag.valueOf("table"),"").attr("name", source.id);
			Element tbody = toreturn.appendElement("tbody");
			for(int icontent=0; icontent<source.content.size(); icontent++)
			{
				HTMLForm currentForm = new HTMLForm(source.content.getAt(icontent).value);
				if(currentForm.type==FieldType.QRY)
				{
					Element contentElement = HTMLForm.getViewHTML(currentForm,masterForm);
					tbody.appendChild(contentElement);
				}
				else throw new FormException("The content of a form Form must be a query Form");
			}
		}
		else throw new FormException("Unrecognizable Form type "+source.type);
		
		return toreturn;
	}
	
	
	
	/**
	 * For regtesting
	 * @param args
	 * @throws FormException
	 * @throws  
	 */
	public static void main(String[] args) throws FormException
	{
		HTMLForm f = new HTMLForm("f1", FieldType.FRM);
		Form q;
		TypedValue val;
		q=new Form("q1", FieldType.QRY);
		q.name="Question 1";
		q.text="What is your name?";
		
		Form valF = new HTMLForm("1", FieldType.VAR);
		
		val=new TypedValue(java.sql.Types.VARCHAR);
		valF.setValue(val);
		q.add(valF);
		f.add(q);
		
		System.out.println(f.generateHTMLView().toString());
		String jsonString = f.toJSONObject().toString();
		System.out.println(jsonString);
		
		
		f = new HTMLForm("f2", FieldType.FRM);
		f.fromJSONObject(new JSONObject(jsonString));
		System.out.println(f.generateHTMLView().toString());
		jsonString = f.toJSONObject().toString();
		System.out.println(jsonString);
		
	}
}
