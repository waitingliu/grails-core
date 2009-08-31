/*
 * Copyright 2004-2005 the original author or authors.
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
package org.codehaus.groovy.grails.web.plugins.support

import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.commons.GrailsTagLibClass
import org.springframework.beans.BeanWrapperImpl
import org.codehaus.groovy.grails.web.pages.GroovyPage
import org.codehaus.groovy.grails.web.pages.TagLibraryLookup;
import org.springframework.web.context.request.RequestContextHolder as RCH
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.taglib.GroovyPageTagBody

/**
 * Provides utility methods used to support meta-programming. In particular commons methods to
 * register tag library method invokations as new methods an a given MetaClass

 * @author Graeme Rocher
 * @since 1.0
 *
 * Created: Sep 7, 2007
 * Time: 9:06:10 AM
 *
 */
class WebMetaUtils {


    /**
     * This creates the difference dynamic methods and properties on the controllers. Most methods
     * are implemented by looking up the current request from the RequestContextHolder (RCH)
     */
    static registerCommonWebProperties(MetaClass mc, GrailsApplication application) {
        def paramsObject = {->
            RCH.currentRequestAttributes().params
        }
        def flashObject = {->
            RCH.currentRequestAttributes().flashScope
        }
        def sessionObject = {->
            RCH.currentRequestAttributes().session
        }
        def requestObject = {->
            RCH.currentRequestAttributes().currentRequest
        }
        def responseObject = {->
            RCH.currentRequestAttributes().currentResponse
        }
        def servletContextObject = {->
            RCH.currentRequestAttributes().servletContext
        }
        def grailsAttrsObject = {->
            RCH.currentRequestAttributes().attributes
        }

        // the params object
        mc.getParams = paramsObject
        // the flash object
        mc.getFlash = flashObject
        // the session object
        mc.getSession = sessionObject
        // the request object
        mc.getRequest = requestObject
        // the servlet context
        mc.getServletContext = servletContextObject
        // the response object
        mc.getResponse = responseObject
        // The GrailsApplicationAttributes object
        mc.getGrailsAttributes = grailsAttrsObject
        // The GrailsApplication object
        mc.getGrailsApplication = {-> RCH.currentRequestAttributes().attributes.grailsApplication }

        mc.getActionName = {->
            RCH.currentRequestAttributes().actionName
        }
        mc.getControllerName = {->
            RCH.currentRequestAttributes().controllerName
        }


    }


    static registerMethodMissingForTags(MetaClass mc, TagLibraryLookup gspTagLibraryLookup, String namespace, String name) {
    	def tagLibBean=gspTagLibraryLookup.lookupTagLibrary(namespace, name)
    	//def lookupTagLib={-> tagLibBean }
    	// for Request scope support, change this
    	// def lookupTagLib={-> gspTagLibraryLookup.lookupTagLibrary(namespace, name) }
    	
        mc."$name" = {Map attrs, Closure body ->
            GroovyPage.captureTagOutput(tagLibBean, name, attrs, body, RCH.currentRequestAttributes())
        }
        mc."$name" = {Map attrs, String body ->
            delegate."$name"(attrs, new GroovyPage.ConstantClosure(body))
        }
        mc."$name" = {Map attrs ->
            GroovyPage.captureTagOutput(tagLibBean, name, attrs, null, RCH.currentRequestAttributes())
        }
        mc."$name" = {Closure body ->
            GroovyPage.captureTagOutput(tagLibBean, name, [:], body, RCH.currentRequestAttributes())
        }
        mc."$name" = {->
            GroovyPage.captureTagOutput(tagLibBean, name, [:], null, RCH.currentRequestAttributes())
        }
    }

    static registerMethodMissingForTags(MetaClass mc, ApplicationContext ctx, GrailsTagLibClass tagLibraryClass, String name) {
        //def tagLibrary = ctx.getBean(tagLibraryClass.fullName)
		TagLibraryLookup gspTagLibraryLookup = ctx.getBean("gspTagLibraryLookup")
		String namespace = tagLibraryClass.namespace ?: GroovyPage.DEFAULT_NAMESPACE
        registerMethodMissingForTags(mc,gspTagLibraryLookup,namespace,name)
    }


}