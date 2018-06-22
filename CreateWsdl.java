package driver.java.program.wsdl;

import static com.predic8.schema.Schema.*;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.predic8.schema.Annotation;
import com.predic8.schema.Element;
import com.predic8.schema.Schema;
import com.predic8.schema.Sequence;
import com.predic8.wsdl.Binding;
import com.predic8.wsdl.BindingOperation;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Fault;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.Port;
import com.predic8.wsdl.PortType;
import com.predic8.wsdl.Service;
import com.predic8.wsdl.WSDLParser;
import com.predic8.wsdl.WSDLParserContext;
import com.predic8.wsdl.creator.WSDLCreator;
import com.predic8.wsdl.creator.WSDLCreatorContext;
import groovy.xml.MarkupBuilder;


public class CreateWsdl {
	/* Adding simple element to the Schema*/
	private static void attachElement(Sequence sq,List<String> elements){
		for(String ele:elements ){
			Element el=sq.newElement(ele);
			el.setMinOccurs("0");
			el.setMaxOccurs("1");
			el.setDefaultValue("");
			//Annotation ant=el.setAnnotation(null).newDocumentation(ele);
		}
	}
	
	/* Adding Complex element to the Schema */
private static void attachComplexElement(Schema sc,List<String> complexList){
		Scanner scr=new Scanner(System.in);
		for(String list:complexList){
			Sequence sq=sc.newElement(list).newComplexType().newSequence();
			System.out.printf("Enter the number of simple element for complex element %s \n",list);
			int n=scr.nextInt();
			List<String> element=new ArrayList<String>();
			System.out.println("Enter the simple elements:");
			for(int i=1;i<=n;i++){
				String ele=scr.next();
				element.add(ele);
			}
			attachElement(sq,element);
			
		}
	}

	/* Creating Scherma for the wsdl */
	private static Schema createSchema(){
		Scanner scr=new Scanner(System.in);
		System.out.println("Enter the namespace for the schema:");
		String ns=scr.next();
		Schema scm=new Schema(ns);
		System.out.println("Enter the number of Complex Element :");
		int n=scr.nextInt();
		System.out.println("Enter the complex elements:");
		List<String> complexList=new ArrayList<String>();
		for(int i=1;i<=n;i++){
			String complexEle=scr.next();
			complexList.add(complexEle);
		}
		attachComplexElement(scm,complexList);
		return scm;
		
	}
	 
	/* creating the wsdl object of type Definitions */
	private static Definitions createDefinition(Schema sc){
		Scanner scr=new Scanner(System.in);
		System.out.println("Enter the target namespace for the wsdl :");
		String tnameSpace=scr.next();
		System.out.println("Enter the name for the service");
		String serviceName=scr.next();
		Definitions wsdl=new Definitions(tnameSpace,serviceName);
		wsdl.addSchema(sc);
		return wsdl;
	}
	
	/* Method addPortType() use to add portType and corresponding operations to the wsdl*/
	private static Definitions addPortType(Definitions wsdl){  
		Scanner scr=new Scanner(System.in);
		System.out.println("Enter the name for the portType:");
		String portTypeName=scr.next();
		PortType pt=wsdl.newPortType(portTypeName);
		System.out.println("Enter the number of Operation :");
		Integer numb=scr.nextInt();
		for(int i=1;i<=numb;i++){
			System.out.printf("Enter the name for the operation %d :\n",i);
			String opname=scr.next();
			Operation op=pt.newOperation(opname);
			System.out.println("Enter the newInput:");
			String newinput=scr.next();
			System.out.println("Enter the newMessage for input :");
			String newmessage=scr.next();
			System.out.println("Enter the parameter value for input:");
			String parameter=scr.next();
			String type="tns:"+newinput;
			op.newInput(newinput).newMessage(newmessage).newPart(parameter, type);
			System.out.println("Enter the newoutput:");
			String newOP=scr.next();
			System.out.println("Enter the newMessage for output :");
			String newOPmessage=scr.next();
			System.out.println("Enter the parameter value for output:");
			String OPparameter=scr.next();
			String OPtype="tns:"+newOP;
			op.newOutput(newOP).newMessage(newOPmessage).newPart(OPparameter, OPtype);
		}
		
		return wsdl;
	}
	/* Binding operations of the wsdl*/
	private static Definitions addBinding(Definitions wsdl){
		Scanner scr=new Scanner(System.in);
		System.out.println("Enter the Binding name :");
		String name=scr.next();
		 Binding bd = wsdl.newBinding(name);
		 PortType pt=(PortType) wsdl.getPortTypes();
		    bd.setType(pt);
		for(Operation s:wsdl.getOperations()){
			String st=""+s;
			BindingOperation bo =  bd.newBindingOperation(st);
			System.out.println("Enter the soapAction for bionding operation :");
			String arg0=scr.next();
			bo.newSOAP11Operation().setSoapAction(arg0);
			bo.newInput().newSOAP11Body();
		    bo.newOutput().newSOAP11Body();
		}
		
		return wsdl;
	}
	 /*Adding services to the wsdl */
	private static Definitions addService(Definitions wsdl){
		Scanner scr=new Scanner(System.in);
		System.out.println("Enter the Service name :");
		String sname=scr.next();
		Service sr=wsdl.newService(sname);
		    Port p=sr.newPort("LoanserviceSOAP11Port_HTTP");
		    Binding bd=(Binding) wsdl.getBindings();
		    p.setBinding(bd);
		    p.newSOAP11Address("http://rhlux1121/cordys/com.alahli.gateway.NCBGateway.wcp?organization=o=system,cn=cordys,cn=validationEnv,o=alahli.com");
		    
		   return wsdl;
	}
	
	private static Definitions createWSDL(){
		Schema sc=createSchema();  // calling Schema creation method createSchema().
		Definitions wsdl=createDefinition(sc);  // calling method createDefinition(sc) , consuming the creatd schema .
		wsdl=addPortType(wsdl);	// calling method addPortType(wsdl) which will add portType to wsdl.
		wsdl=addBinding(wsdl);	// calling method which will  Bind the operations .				
		wsdl=addService(wsdl);  // calling method add services .
		return wsdl;
	}
	
	 private static void dumpWSDL(Definitions wsdl)  {
		    StringWriter writer = new StringWriter();
		    WSDLCreator creator = new WSDLCreator();
		    creator.setBuilder(new MarkupBuilder(writer));
		    wsdl.create(creator, new WSDLCreatorContext());
		    
		    try{    
	            FileOutputStream fout=new FileOutputStream("D:/wsdl/LoanService2.wsdl");    
	            String s=writer.toString();
	            byte[] b=s.getBytes();  //converting string into byte array    
	            fout.write(b);   
	            fout.close();    
	           }catch(Exception e){System.out.println(e);} 
		    
		    //System.out.println(writer);
		    System.out.println("WSDL Updated ...!");
		  }
	 
	/*main functions */
	public static void main(String[] args){
		
		/**/
		dumpWSDL(createWSDL());
	}
}
