package org.giavacms.paypal.controller.request;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.giavacms.paypal.controller.session.ShoppingCartSessionController;
import org.giavacms.paypal.model.ShoppingCart;
import org.giavacms.paypal.producer.PaypallProducer;
import org.giavacms.paypal.repository.ShoppingCartRepository;
import org.giavacms.paypal.util.PaypalUtils;
import org.jboss.logging.Logger;

import com.paypal.core.rest.PayPalRESTException;

@Named
@RequestScoped
public class PaymentRequestController
{
   private String payerId;
   private String guid;

   @Inject
   ShoppingCartRepository shoppingCartRepository;

   @Inject
   ShoppingCartSessionController shoppingCartSessionController;

   @Inject
   PaypallProducer paypallProducer;

   Logger logger = Logger.getLogger(getClass().getName());

   public PaymentRequestController()
   {
   }

   @PostConstruct
   public void postConstruct()
   {
      for (String key : FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().keySet())
      {
         Object value = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(key);
         System.out.println("key: " + key + " - value: " + value);
      }

      if (FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("guid") != null
               && !FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("guid")
                        .isEmpty())
      {
         setGuid(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("guid"));
      }

      if (FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("payerId") != null
               && !FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("payerId")
                        .isEmpty())
      {
         setGuid(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("payerId"));
      }

   }

   public String verify()
   {
      if (getPayerId() != null && !getPayerId().isEmpty() && getGuid() != null && !getGuid().isEmpty())
      {
         // verifico che un ordine con quel guid exists
         ShoppingCart shoppingCart = shoppingCartRepository.find(Long.valueOf(getGuid()));
         if (shoppingCart != null)
            try
            {
               PaypalUtils.end(getPayerId(), getGuid(), paypallProducer.getPaypalConfiguration());
               shoppingCart.setDataEnd(new Date());
               shoppingCart.setPayed(true);
               shoppingCartRepository.update(shoppingCart);
               shoppingCartSessionController.resetShoppingCart();
               logger.info("PaymentRequestController.verify: completo!!!");
            }
            catch (PayPalRESTException e)
            {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         else
            logger.info("PaymentRequestController.verify: NON ESISTE SHOPPING CART!!!");
      }
      else
      {
         shoppingCartSessionController.resetShoppingCart();
         logger.info("PaymentRequestController.verify: parametri incompleti!!! - payerId:" + getPayerId() + " - guid: "
                  + getGuid());
      }
      return "";
   }

   public String reset()
   {
      if (getGuid() != null && !getGuid().isEmpty())
      {
         // verifico che un ordine con quel guid exists
         ShoppingCart shoppingCart = shoppingCartRepository.find(Long.valueOf(getGuid()));
         if (shoppingCart != null)
            try
            {
               shoppingCart.setDataEnd(new Date());
               shoppingCart.setPayed(false);
               shoppingCartRepository.update(shoppingCart);
               shoppingCartSessionController.resetShoppingCart();
               logger.info("PaymentRequestController.reset: completo!!!");
            }
            catch (Exception e)
            {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         else
            logger.info("PaymentRequestController.reset: NON ESISTE SHOPPING CART!!!");
      }
      else
      {
         shoppingCartSessionController.resetShoppingCart();
         logger.info("PaymentRequestController.reset: parametri incompleti!!! - payerId:" + getPayerId() + " - guid: "
                  + getGuid());
      }
      return "";
   }

   public String getPayerId()
   {
      return payerId;
   }

   public void setPayerId(String payerId)
   {
      this.payerId = payerId;
   }

   public String getGuid()
   {
      return guid;
   }

   public void setGuid(String guid)
   {
      this.guid = guid;
   }
}