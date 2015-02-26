package grails.plugins.quartz

import grails.artefact.Artefact
import spock.lang.Specification

class QuartzJobTraitInjectorSpec extends Specification {

    void 'test that the job trait is applied'() {
        expect:
        QuartzJob.isAssignableFrom TraitTestJob
    }
}

@Artefact('Job')
class TraitTestJob {}
