/*
 * Copyright 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.juddi.v2.tck;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uddi.api_v2.BindingTemplates;
import org.uddi.api_v2.BusinessEntity;
import org.uddi.api_v2.BusinessInfos;
import org.uddi.api_v2.BusinessList;
import org.uddi.api_v2.CategoryBag;
import org.uddi.api_v2.Contacts;
import org.uddi.api_v2.DeleteBusiness;
import org.uddi.api_v2.Description;
import org.uddi.api_v2.FindBusiness;
import org.uddi.api_v2.FindQualifiers;
import org.uddi.api_v2.FindService;
import org.uddi.api_v2.FindTModel;
import org.uddi.api_v2.KeyedReference;
import org.uddi.api_v2.Name;
import org.uddi.api_v2.ServiceInfos;
import org.uddi.api_v2.ServiceList;
import org.uddi.api_v2.TModelList;
import org.uddi.v2_service.*;


/**
 * Common Utilities for TCK tests
 *
 * @author Alex O'Ree
 */
public class TckCommon {

        private static Log logger = LogFactory.getLog(TckCommon.class);
//<editor-fold defaultstate="collapsed" desc="Some basic util functions to print out the data structure">

        /**
         * Converts category bags of tmodels to a readable string
         *
         * @param categoryBag
         * @return human readable category bag
         */
        public static String CatBagToString(CategoryBag categoryBag) {
                StringBuilder sb = new StringBuilder();
                if (categoryBag == null) {
                        return "no data";
                }
                for (int i = 0; i < categoryBag.getKeyedReference().size(); i++) {
                        sb.append(KeyedReferenceToString(categoryBag.getKeyedReference().get(i)));
                }
                return sb.toString();
        }

        public static String KeyedReferenceToString(KeyedReference item) {
                StringBuilder sb = new StringBuilder();
                sb.append("Key Ref: Name=").
                        append(item.getKeyName()).
                        append(" Value=").
                        append(item.getKeyValue()).
                        append(" tModel=").
                        append(item.getTModelKey()).
                        append(System.getProperty("line.separator"));
                return sb.toString();
        }

        public static void PrintContacts(Contacts contacts) {
                if (contacts == null) {
                        return;
                }
                for (int i = 0; i < contacts.getContact().size(); i++) {
                        System.out.println("Contact " + i + " type:" + contacts.getContact().get(i).getUseType());

                                System.out.println("Name: " + contacts.getContact().get(i).getPersonName());
                        
                        for (int k = 0; k < contacts.getContact().get(i).getEmail().size(); k++) {
                                System.out.println("Email: " + contacts.getContact().get(i).getEmail().get(k).getValue());
                        }
                        for (int k = 0; k < contacts.getContact().get(i).getAddress().size(); k++) {
                                System.out.println("Address sort code " + contacts.getContact().get(i).getAddress().get(k).getSortCode());
                                System.out.println("Address use type " + contacts.getContact().get(i).getAddress().get(k).getUseType());
                                System.out.println("Address tmodel key " + contacts.getContact().get(i).getAddress().get(k).getTModelKey());
                                for (int x = 0; x < contacts.getContact().get(i).getAddress().get(k).getAddressLine().size(); x++) {
                                        System.out.println("Address line value " + contacts.getContact().get(i).getAddress().get(k).getAddressLine().get(x).getValue());
                                        System.out.println("Address line key name " + contacts.getContact().get(i).getAddress().get(k).getAddressLine().get(x).getKeyName());
                                        System.out.println("Address line key value " + contacts.getContact().get(i).getAddress().get(k).getAddressLine().get(x).getKeyValue());
                                }
                        }
                        for (int k = 0; k < contacts.getContact().get(i).getDescription().size(); k++) {
                                System.out.println("Desc: " + contacts.getContact().get(i).getDescription().get(k).getValue());
                        }
                        for (int k = 0; k < contacts.getContact().get(i).getPhone().size(); k++) {
                                System.out.println("Phone: " + contacts.getContact().get(i).getPhone().get(k).getValue());
                        }
                }

        }

        /**
         * This function is useful for translating UDDI's somewhat complex data
         * format to something that is more useful.
         *
         * @param bindingTemplates
         */
        public static void PrintBindingTemplates(BindingTemplates bindingTemplates) {
                if (bindingTemplates == null) {
                        return;
                }
                for (int i = 0; i < bindingTemplates.getBindingTemplate().size(); i++) {
                        System.out.println("Binding Key: " + bindingTemplates.getBindingTemplate().get(i).getBindingKey());

                        if (bindingTemplates.getBindingTemplate().get(i).getAccessPoint() != null) {
                                System.out.println("Access Point: " + bindingTemplates.getBindingTemplate().get(i).getAccessPoint().getValue() + " type " + bindingTemplates.getBindingTemplate().get(i).getAccessPoint().getURLType().value());
                        }

                        if (bindingTemplates.getBindingTemplate().get(i).getHostingRedirector() != null) {
                                System.out.println("Hosting Redirection: " + bindingTemplates.getBindingTemplate().get(i).getHostingRedirector().getBindingKey());
                        }
                }
        }

        public static void PrintBusinessInfo(BusinessInfos businessInfos) {
                if (businessInfos == null) {
                        System.out.println("No data returned");
                } else {
                        for (int i = 0; i < businessInfos.getBusinessInfo().size(); i++) {
                                System.out.println("===============================================");
                                System.out.println("Business Key: " + businessInfos.getBusinessInfo().get(i).getBusinessKey());
                                System.out.println("Name: " + ListToString(businessInfos.getBusinessInfo().get(i).getName()));

                                System.out.println("Name: " + ListToDescString(businessInfos.getBusinessInfo().get(i).getDescription()));
                                System.out.println("Services:");
                                PrintServiceInfo(businessInfos.getBusinessInfo().get(i).getServiceInfos());
                        }
                }
        }

        public static String ListToString(List<Name> name) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < name.size(); i++) {
                        sb.append(name.get(i).getValue()).append(" ");
                }
                return sb.toString();
        }

        public static String ListToDescString(List<Description> name) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < name.size(); i++) {
                        sb.append(name.get(i).getValue()).append(" ");
                }
                return sb.toString();
        }

        public static void PrintServiceInfo(ServiceInfos serviceInfos) {
                for (int i = 0; i < serviceInfos.getServiceInfo().size(); i++) {
                        System.out.println("-------------------------------------------");
                        System.out.println("Service Key: " + serviceInfos.getServiceInfo().get(i).getServiceKey());
                        System.out.println("Owning Business Key: " + serviceInfos.getServiceInfo().get(i).getBusinessKey());
                        System.out.println("Name: " + ListToString(serviceInfos.getServiceInfo().get(i).getName()));
                }
        }

        public static void PrintBusinessDetails(List<BusinessEntity> businessDetail) {


                for (int i = 0; i < businessDetail.size(); i++) {
                        System.out.println("Business Detail - key: " + businessDetail.get(i).getBusinessKey());
                        System.out.println("Name: " + ListToString(businessDetail.get(i).getName()));
                        System.out.println("CategoryBag: " + CatBagToString(businessDetail.get(i).getCategoryBag()));
                        PrintContacts(businessDetail.get(i).getContacts());
                }
        }

        /**
         * use this for clean up actions after running tests. if an exception is
         * raised, it will only be logged
         *
         * @param key
         * @param authInfo
         * @param publish
         */
        public static void DeleteBusiness(String key, String authInfo, Publish publish) {
                if (key == null) {
                        return;
                }
                try {
                        DeleteBusiness db = new DeleteBusiness();
                        db.setGeneric("2.0");
                        db.setAuthInfo(authInfo);
                        db.getBusinessKey().add(key);
                        publish.deleteBusiness(db);
                } catch (Exception ex) {
                        logger.warn("failed to delete business " + key + " " + ex.getMessage());
                        logger.debug("failed to delete business " + key + " " + ex.getMessage(), ex);
                }
        }

        
        /**
         * returns true if the System Property "debug" is equal to "true"
         * @return true/false
         */
        public static boolean isDebug() {
                boolean serialize = false;
                try {
                        if (System.getProperty("debug") != null
                                && System.getProperty("debug").equalsIgnoreCase("true")) {
                                serialize = true;
                        }
                } catch (Exception ex) {
                }
                return serialize;
        }
        
        
         public static String DumpAllServices(String authinfo, Inquire inquiry) {
                StringBuilder sb = new StringBuilder();
                FindService fs = new FindService();
                //fs.setAuthInfo(authinfo);
                //fs.setFindQualifiers(new FindQualifiers());
                //fs.getFindQualifiers().getFindQualifier().add("approximateMatch");
                fs.getName().add(new Name("%", null));
                try {
                        ServiceList findService = inquiry.findService(fs);
                        if (findService.getServiceInfos() == null) {
                                return ("NO SERVICES RETURNED!");
                        } else {
                                for (int i = 0; i < findService.getServiceInfos().getServiceInfo().size(); i++) {
                                        sb.append(findService.getServiceInfos().getServiceInfo().get(i).getName().get(0).getValue()).append(" lang=").append(findService.getServiceInfos().getServiceInfo().get(i).getName().get(0).getLang()).append(" ").append(findService.getServiceInfos().getServiceInfo().get(i).getServiceKey()).append(" ").append(findService.getServiceInfos().getServiceInfo().get(i).getBusinessKey()).append(
                                                System.getProperty("line.separator"));
                                }
                        }
                } catch (Exception ex) {
                        sb.append(ex.getMessage());
                }
                return sb.toString();
        }

         
        public static String DumpAllTModels(String authinfo, Inquire inquriy) {
                StringBuilder sb = new StringBuilder();
                FindTModel fs = new FindTModel();
                //fs.setAuthInfo(authinfo);
                //fs.setFindQualifiers(new FindQualifiers());
                //fs.getFindQualifiers().getFindQualifier().add("approximateMatch");
                fs.setName(new Name("%", null));
                fs.setGeneric("2.0");
                try {
                        TModelList findService = inquriy.findTModel(fs);
                        if (findService.getTModelInfos()== null) {
                                return ("NO TMODELS RETURNED!");
                        } else {
                                for (int i = 0; i < findService.getTModelInfos().getTModelInfo().size(); i++) {
                                        sb.append(findService.getTModelInfos().getTModelInfo().get(i).getName().getValue()).
                                                append(" lang=").append(findService.getTModelInfos().getTModelInfo().get(i).getName().getLang()).
                                                append(" ").append(findService.getTModelInfos().getTModelInfo().get(i).getTModelKey())
                                                .append(System.getProperty("line.separator"));
                                }
                        }
                } catch (Exception ex) {
                        return ex.getMessage();
                }
                return sb.toString();
        }         
        public static String DumpAllBusinesses(String authinfo, Inquire inquriy) {
                StringBuilder sb = new StringBuilder();
                FindBusiness fs = new FindBusiness();
                //fs.setAuthInfo(authinfo);
                //fs.setFindQualifiers(new FindQualifiers());
                //fs.getFindQualifiers().getFindQualifier().add("approximateMatch");
                fs.getName().add(new Name("%", null));
                fs.setGeneric("2.0");
                try {
                        BusinessList findService = inquriy.findBusiness(fs);
                        if (findService.getBusinessInfos() == null) {
                                return ("NO BUSINESSES RETURNED!");
                        } else {
                                for (int i = 0; i < findService.getBusinessInfos().getBusinessInfo().size(); i++) {
                                        sb.append(findService.getBusinessInfos().getBusinessInfo().get(i).getName().get(0).getValue()).
                                                append(" lang=").append(findService.getBusinessInfos().getBusinessInfo().get(i).getName().get(0).getLang()).
                                                append(" ").append(findService.getBusinessInfos().getBusinessInfo().get(i).getBusinessKey())
                                                .append(System.getProperty("line.separator"));
                                }
                        }
                } catch (Exception ex) {
                        return ex.getMessage();
                }
                return sb.toString();
        }

}
