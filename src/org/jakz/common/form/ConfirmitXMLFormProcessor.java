package org.jakz.common.form;

import java.io.InputStream;

import org.jakz.common.form.Form.FieldType;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;

public class ConfirmitXMLFormProcessor 
{
	private InputStream sourceIS;
	private Form targetForm;

	private Integer languageCode;
	private Boolean fallbackToFirstLanguageFound;
	
	public ConfirmitXMLFormProcessor()
	{
		fallbackToFirstLanguageFound = true;
	}
	
	protected void addStandardResponseColumns()
	{
		//add standard response columns
		String dateFormat = "yyyy-MM-dd HH:mm:ss";
		Form q;
		if(!targetForm.getHasContent())
			q = targetForm.addQuery("0");
		else
			q = targetForm.content.getValueAt(0);
		
		q.optVariable("responseid", java.sql.Types.BIGINT);
		q.optVariable("respid", java.sql.Types.BIGINT);
		q.optVariable("interview_start", java.sql.Types.TIMESTAMP).setValueParseFormat(dateFormat);
		q.optVariable("interview_end", java.sql.Types.TIMESTAMP).setValueParseFormat(dateFormat);
		q.optVariable("status", java.sql.Types.VARCHAR);
	}
	
	public ConfirmitXMLFormProcessor setSource(InputStream nSource)
	{
		sourceIS=nSource;
		return this;
	}
	
	public ConfirmitXMLFormProcessor setTarget(Form nTarget)
	{
		targetForm=nTarget;
		return this;
	}
	
	public Form getTargetForm() {return targetForm;}
	
	public ConfirmitXMLFormProcessor setLanguageCode(Integer nLanguageCode)
	{
		languageCode=nLanguageCode;
		return this;
	}
	
	public ConfirmitXMLFormProcessor setFallbackToFirstLanguageFound(Boolean nFallbackToFirstLanguageFound)
	{
		fallbackToFirstLanguageFound=nFallbackToFirstLanguageFound;
		return this;
	}
	
	public ConfirmitXMLFormProcessor populateFormDataFromFile() throws FormException
	{
		
		if(targetForm==null)
		{
			targetForm = new Form("ConfirmitXMLFormProcessorForm");
		}
		
		addStandardResponseColumns();
		
		try
		{
			Builder xmlParser = new Builder();
			Document doc = xmlParser.build(sourceIS);
			Element root = doc.getRootElement(); //Project
			Element questionnarieElement = root.getFirstChildElement("Questionnaire");
			Element routingNodes = questionnarieElement.getFirstChildElement("Routing").getFirstChildElement("Nodes");
			Element blocksNodes = questionnarieElement.getFirstChildElement("Blocks").getFirstChildElement("Nodes");
			Form templateQueryRow = null;
			if(targetForm.getHasContent())
				templateQueryRow=targetForm.content.getValueAt(0);
			else
			{
				templateQueryRow=new Form(targetForm.id, Form.FieldType.QRY);
				targetForm.add(templateQueryRow);
			}
			
			for(int i=0; i<routingNodes.getChildCount(); i++)
			{
				populateFormDataFromQuestionElement(templateQueryRow,routingNodes.getChild(i));
				
			}
			
			for(int i=0; i<blocksNodes.getChildCount(); i++)
			{
				populateFormDataFromQuestionElement(templateQueryRow,blocksNodes.getChild(i));
			}
		}
		catch (Exception e)
		{
			throw new FormException("Could not populate Form from Confirmit xml",e);
		}
		return this;
	}
	
	//TODO - can be improved. does not cover all question types?
	protected void populateFormDataFromQuestionElement(Form templateQueryRow, Node nQuestionElement) throws FormException
	{
		Element qe = (Element) nQuestionElement;
		String localName = qe.getLocalName();
		
		
		
		Form targetVariable = new Form("ConfirmitXMLFormProcessor_templateVariable", FieldType.VAR);
		
		String questionId = null;
		
		Element nameElement = qe.getFirstChildElement("Name");
		if(nameElement!=null)
			questionId=nameElement.getValue();
		
		//attributes
		String entityId = qe.getAttributeValue("EntityId");
		String variableType = qe.getAttributeValue("VariableType");
		String questionCategory = qe.getAttributeValue("QuestionCategory");
		String sDefaultValue = qe.getAttributeValue("DefaultValue");
		String sPrecision = qe.getAttributeValue("Precision");
		String sScale = qe.getAttributeValue("Scale");
		String sRows = qe.getAttributeValue("Rows");
		String sNumeric = qe.getAttributeValue("Numeric");
		String lowerLimitType = qe.getAttributeValue("LowerLimitType");
		String upperLimitType = qe.getAttributeValue("UpperLimitType");
		
		String formTextTitle = null;
		String formTextText = null;
		String formTextInstruction = null;
		
		Element formTextsElement = qe.getFirstChildElement("FormTexts");
		if(formTextsElement!=null)
		{
			Elements formTexts = formTextsElement.getChildElements("FormText");
			for(int i=0; i<formTexts.size(); i++)
			{
				Element formText = formTexts.get(i);
				String language = formText.getAttributeValue("Language");
				if(
						(language!=null&&languageCode!=null&&languageCode==Integer.parseInt(language))
				||
						(languageCode==null&&fallbackToFirstLanguageFound!=null)
				)
				{
					if(formText.getFirstChildElement("Title")!=null)
						formTextTitle=formText.getFirstChildElement("Title").getValue();
					if(formText.getFirstChildElement("Text")!=null)
						formTextText=formText.getFirstChildElement("Text").getValue();
					if(formText.getFirstChildElement("Instruction")!=null)
						formTextInstruction=formText.getFirstChildElement("Instruction").getValue();
					break;
				}
			}
		}
		
		
		try
		{
			//populate targetVariable
			targetVariable.setId(questionId);
			targetVariable.parameter.put("EntityId",entityId);
			targetVariable.name=formTextTitle;
			targetVariable.text=formTextText;
			targetVariable.instruction=formTextInstruction;
			
			if(localName.equals("Folder")||localName.equals("Page")||localName.equals("StartBlock")||localName.equals("CallableBlock")||localName.equals("EndBlock"))
			{
				Element nodes = ((Element)nQuestionElement).getFirstChildElement("Nodes");
				for(int i=0;nodes!=null&&i<nodes.getChildCount(); i++)
				{
					populateFormDataFromQuestionElement(templateQueryRow,nodes.getChild(i));
				}
				return;
			}
			else if(localName.equals("Condition"))
			{
				Element nodes = ((Element)nQuestionElement).getFirstChildElement("TrueNodes");
				for(int i=0; nodes!=null&&i<nodes.getChildCount(); i++)
				{
					populateFormDataFromQuestionElement(templateQueryRow,nodes.getChild(i));
				}
				
				nodes = ((Element)nQuestionElement).getFirstChildElement("FalseNodes");
				for(int i=0; nodes!=null&&i<nodes.getChildCount(); i++)
				{
					populateFormDataFromQuestionElement(templateQueryRow,nodes.getChild(i));
				}
				return;
			}
			else if(localName.equals("Open")) //text or numeric
			{
				if(sNumeric==null)
				{
					//string
					targetVariable.setValue(java.sql.Types.NVARCHAR);
				}
				else
				{
					//numeric
					if(sScale==null)
						targetVariable.setValue(java.sql.Types.BIGINT);
					else
						targetVariable.setValue(java.sql.Types.DOUBLE);
				}
			}
			else if(localName.equals("Single")||localName.equals("Multi"))
			{
				targetVariable.setValue(java.sql.Types.INTEGER);
				Element answersElement;
				if(localName.equals("Single"))
				{
					answersElement = qe.getFirstChildElement("SingleAnswers");
				}
				else
				{
					answersElement = qe.getFirstChildElement("MultiAnswers");
				}
				
				if(answersElement!=null)
				{
					Elements answers = answersElement.getChildElements("Answer");
					for(int iAnswer=0; iAnswer<answers.size(); iAnswer++)
					{
						Element answer = answers.get(iAnswer);
						String sAnswerPrecode = answer.getAttributeValue("Precode");
						String sAnswerExclusive=answer.getAttributeValue("Exclusive");
						String sAnswerOther=answer.getAttributeValue("Other");
						Element answerTextsElement = answer.getFirstChildElement("Texts");
						Elements answerTexts = answerTextsElement.getChildElements("Text");
						String answerTextString=null;
						
						
						for(int iText=0; iText<answerTexts.size(); iText++)
						{
							Element answerText = answerTexts.get(iText);
							String language = answerText.getAttributeValue("Language");
							if(
									(language!=null&&languageCode!=null&&languageCode==Integer.parseInt(language))
							||
									(languageCode==null&&fallbackToFirstLanguageFound!=null)
							)
							{
								answerTextString=answerText.getValue();
								break;
							}
						}
						
						Form alternativeForm = targetVariable.addAlternative(sAnswerPrecode).setValue(java.sql.Types.NVARCHAR).setText(answerTextString);
						alternativeForm.getValue().setSizeLimit(sAnswerPrecode.length());
						alternativeForm.alternativeHasOtherField=sAnswerOther!=null;
						alternativeForm.parameter.put("Exclusive",sAnswerExclusive);
						alternativeForm.nullable=true;
					}
				}
				
			}
			else
			{
				//OTHER ELEMENTS
				return; //do not add element
			}
			
			templateQueryRow.add(targetVariable);
		}
		catch (Exception e)
		{
			throw new FormException("Error parsing "+localName+" "+questionId, e);
		}
	}
	
}
