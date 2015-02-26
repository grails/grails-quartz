/*
 * Copyright 2015 the original author or authors.
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
package grails.plugins.quartz

import grails.compiler.traits.TraitInjector
import groovy.transform.CompileStatic
import org.grails.io.support.GrailsResourceUtils

import java.util.regex.Pattern

@CompileStatic
class QuartzJobTraitInjector implements TraitInjector {

    static Pattern JOB_PATTERN = Pattern.compile(".+/${GrailsResourceUtils.GRAILS_APP_DIR}/jobs/(.+)Job\\.groovy")

    @Override
    Class getTrait() {
        QuartzJob
    }

    @Override
    String[] getArtefactTypes() {
        [DefaultGrailsJobClass.JOB] as String[]
    }

    @Override
    boolean shouldInject(URL url) {
        url != null && JOB_PATTERN.matcher(url.file).find()
    }
}
