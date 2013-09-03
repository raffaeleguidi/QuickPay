package controllers
 
import play.api._
import play.api.mvc._
import models.Purchase
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._
import play.api.libs.ws.WS
import play.api.libs.concurrent.Execution.Implicits._
import java.net.{URLDecoder, URLEncoder}

import views._

object Paypal extends Controller {
 
  //val url = "https://www.paypal.com/cgi-bin/webscr?cmd=_notify-validate&";
	val url = "https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_notify-validate&"
	  
	  
	val ipnForm = Form(
	  tuple(
	    "txn_type" -> nonEmptyText,
	    "payment_status" -> nonEmptyText,
	    "txn_id" -> longNumber(min=1)
	  )
	)

    
    //http://stackoverflow.com/questions/12711713/how-to-implement-a-paypal-ipn-controller-in-java-using-play-2-0

	def urlEncode(in : String) = URLEncoder.encode(in, "UTF-8")
	def paramsToUrlParams(params: List[(String, String)]): String = params.map {
		case (n, v) => urlEncode(n) + "=" + urlEncode(v)
	}.mkString("&")
	
	def test = Action {
	  Ok(html.testIpn())
	}

	
	def ipn = Action(parse.tolerantText) { implicit request =>
//	  see https://github.com/theon/scala-uri
	  
//	  val txn_id = uri.query.params.get("txn_id").get(0)
//	  val txn_type = uri.query.params.get("txn_type").get(0)
//	  val payment_status = uri.query.params.get("payment_status").get(0)
//	  Logger.info(s"Verifying txn_type=$txn_type, payment_status=$payment_status, txn_id=$txn_id")
//
//	  Logger.info(str)
	  //val list = URLDecoder.decode("txn_id=1&txn_type=web%20transaction", "UTF-8").split("&")
	  val list = URLDecoder.decode(request.body, "UTF-8").split("&")
	  list.foreach{ case (v) =>
        //Logger.info(s"v=$v")
        val pair = v.split("=")
        Logger.info(pair(0) + " = ") // + pair(1))
      }
	  Ok("")
//	  
//
//	  
//		//val (txn_type, payment_status, txn_id) = ipnForm.bindFromRequest.get
//	  	Async {
//			//Logger.info(s"Verifying txn_type=$txn_type, payment_status=$payment_status, txn_id=$txn_id")
////			Logger.info(s"request.body=$request.body.asText.toString")
//			val body = request.body
//			Logger.info(body)
////		    WS.url(url).post(s"txn_id=$txn_id").map { response =>
//		    WS.url(url).post(body).map { response =>
//		      //Logger.info(s"txn_id $txn_id is " + response.body)
//		      Ok(s"Response for txn_id $txn_id:  " + response.body)
//		      //Ok(response.body)
//		    }
//		}  
	}

	def ipn2 = Action { implicit request =>
		val (txn_type, payment_status, txn_id) = ipnForm.bindFromRequest.get
		Logger.info(s"Verifying txn_type=$txn_type, payment_status=$payment_status, txn_id=$txn_id")
		Ok("done")
	}
	
	
 /*
  * 
  * https://cms.paypal.com/cms_content/GB/en_GB/files/developer/IPN_JAVA_JSP.txt
  * 
  * // Java JSP

<%@ page import="java.util.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.io.*" %>

<%
// read post from PayPal system and add 'cmd'
Enumeration en = request.getParameterNames();
String str = "cmd=_notify-validate";
while(en.hasMoreElements()){
String paramName = (String)en.nextElement();
String paramValue = request.getParameter(paramName);
str = str + "&" + paramName + "=" + URLEncoder.encode(paramValue);
}

// post back to PayPal system to validate
// NOTE: change http: to https: in the following URL to verify using SSL (for increased security).
// using HTTPS requires either Java 1.4 or greater, or Java Secure Socket Extension (JSSE)
// and configured for older versions.
URL u = new URL("https://www.paypal.com/cgi-bin/webscr");
URLConnection uc = u.openConnection();
uc.setDoOutput(true);
uc.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
PrintWriter pw = new PrintWriter(uc.getOutputStream());
pw.println(str);
pw.close();

BufferedReader in = new BufferedReader(
new InputStreamReader(uc.getInputStream()));
String res = in.readLine();
in.close();

// assign posted variables to local variables
String itemName = request.getParameter("item_name");
String itemNumber = request.getParameter("item_number");
String paymentStatus = request.getParameter("payment_status");
String paymentAmount = request.getParameter("mc_gross");
String paymentCurrency = request.getParameter("mc_currency");
String txnId = request.getParameter("txn_id");
String receiverEmail = request.getParameter("receiver_email");
String payerEmail = request.getParameter("payer_email");

check notification validation
if(res.equals("VERIFIED")) {
// check that paymentStatus=Completed
// check that txnId has not been previously processed
// check that receiverEmail is your Primary PayPal email
// check that paymentAmount/paymentCurrency are correct
// process payment
}
else if(res.equals("INVALID")) {
// log for investigation
}
else {
// error
}
%>*/
	
	
//  def ipn = Action(parse.raw) {request =>
//    request.body.asBytes() match {
//      case Some(arrBytes) => {
//        val queryString = new String(arrBytes);
//        Logger.info("Paypal request: " + queryString)
//        Logger.info("Verifying request: " + url + queryString)
// 
//        // for some reason I always get invalid if I use play.api.lib.ws.WS instead...
//        val client = new HttpClient
//        val methodGet: GetMethod = new GetMethod(url + queryString)
//        client.executeMethod(methodGet)
//        val response = methodGet.getResponseBodyAsString
//        methodGet.releaseConnection
//        if (response.trim != "VERIFIED") {
//          logAndFail("Couldn't verify paypal call", "Verification response: " + response.trim)
//        } else {
//          val res = queryString.split('&') map { str =>
//            val pair = str.split('=')
//            if (pair.length == 2)
//              (pair(0) -> pair(1))
//            else
//              (pair(0) -> "")
//          } toMap;
//          if (res.get("payment_status").exists(_ == "Completed")) {
//            res.get("custom") match {
//              case Some(custom) => custom.split("_").toList match {
//                //case List("purchase", "id", id) => {
//                  Purchases.findById(id).map { purchase =>
//                    
//				      Ok("ok")
//				    
//                  }
//                //}
//                case _ => throw new RuntimeException("invalid custom payload")
//              }
//              case None => logAndFail("custom var not available")
//            }
//          } 
//        } 
//      }
//      case None => logAndFail("Couldn't process paypal ipn")
//    }
//  }
}
