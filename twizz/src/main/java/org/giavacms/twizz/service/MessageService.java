package org.giavacms.twizz.service;

import java.util.logging.Logger;

import javax.ejb.Asynchronous;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.giavacms.twizz.model.QuizCompetitor;
import org.twiliofaces.cdi.doers.Sender;
import org.twiliofaces.inject.configuration.TwilioAccount;

@Stateless
@LocalBean
public class MessageService
{

   Logger logger = Logger.getLogger(getClass().getName());

   @Inject
   @TwilioAccount(accountName = "sms")
   Sender sender;

   @Asynchronous
   public void send(QuizCompetitor quizCompetitor)
   {
      try
      {
         StringBuffer body = new StringBuffer();
         body.append("Hey " + quizCompetitor.getFullName() + ", your score is: " + quizCompetitor.getScore()
                  + " points. powered by http://twizz.me");
         String sid = sender.to(quizCompetitor.getPhone()).body(body.toString()).simpleSend();
         logger.info("SMS ID: " + sid);
      }
      catch (Throwable e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
}
