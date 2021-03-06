package org.giavacms.base.controller;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.giavacms.base.controller.request.PageRequestController;
import org.giavacms.base.model.Page;
import org.giavacms.base.pojo.I18nRequestParams;
import org.giavacms.base.producer.I18nRequestParamsProducer;
import org.giavacms.base.repository.PageRepository;
import org.giavacms.common.controller.AbstractRequestController;
import org.giavacms.common.util.BeanUtils;

public abstract class AbstractPageRequestController<T extends Page> extends AbstractRequestController<T>
{

   private static final long serialVersionUID = 1L;

   @Inject
   Instance<I18nRequestParams> i18nRequestParams;

   @Inject
   I18nRequestParamsProducer i18nRequestParamsProducer;

   @Inject
   PageRequestController pageRequestController;

   @Inject
   PageRepository pageRepository;

   Page basePage = null;

   @Override
   protected void initHttpParams()
   {
      super.initHttpParams();
      handleI18N();
   }

   protected abstract void handleI18N();

   public I18nRequestParams getI18nRequestParams()
   {
      if (i18nRequestParams == null)
      {
         logger.info("Attenzione: ancora non funziona l'inject diretta degli I18nRequestParams. Per ottenereli recuperiamo il loro producer ");
         if (i18nRequestParamsProducer == null)
         {
            logger.info("Attenzione: anche l'del loro producer non va. Li recupero tramite bean utils.");
            i18nRequestParamsProducer = BeanUtils.getBean(I18nRequestParamsProducer.class);
         }
         return i18nRequestParamsProducer.getI18nRequestParams();
      }
      else
      {
         return i18nRequestParams.get();
      }
   }

   @Override
   public String getIdParam()
   {
      throw new RuntimeException("Should not be called. getIdValue override implement");
   }

   @Override
   protected Object getIdValue()
   {
      return pageRequestController.getElement().getId();
   }

   protected Page getBasePage()
   {
      if (basePage == null)
      {
         if (pageRequestController.getElement() != null && pageRequestController.getElement().getId() != null)
         {
            if (pageRequestController.getElement().getTitle() != null)
            {
               basePage = pageRequestController.getElement();
            }
            else
            {
               basePage = pageRepository.find(pageRequestController.getElement().getId());
            }
         }
      }
      return basePage;
   }

   @Override
   protected void initSearch()
   {
      // elementi della stessa lingua della pagina base
      if (getBasePage() != null && getBasePage().getLang() > 0)
      {
         super.getSearch().getObj().setLang(getBasePage().getLang());
      }
   }
   
}
