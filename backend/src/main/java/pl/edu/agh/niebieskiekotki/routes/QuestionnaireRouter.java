package pl.edu.agh.niebieskiekotki.routes;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.web.bind.annotation.*;
import pl.edu.agh.niebieskiekotki.DataBaseMock;
import pl.edu.agh.niebieskiekotki.HibernateAdapter;
import pl.edu.agh.niebieskiekotki.entitites.Questionnaire;
import pl.edu.agh.niebieskiekotki.entitites.QuestionnaireTerm;
import pl.edu.agh.niebieskiekotki.entitites.Term;
import pl.edu.agh.niebieskiekotki.errorsHandling.exceptions.NotFoundException;
import pl.edu.agh.niebieskiekotki.views.AddQuestionnaireView;
import pl.edu.agh.niebieskiekotki.views.QuestionnaireDetail;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
public class QuestionnaireRouter {

    @PersistenceContext
    private EntityManager entityManager;

    List<Questionnaire> questionnaires = new ArrayList<>();


    @GetMapping(value="/questionnaires")
    public List<Questionnaire> GetAll(){
        HibernateAdapter.entityManager = entityManager;
        return new HibernateAdapter().getAll(Questionnaire.class);
    }

    @GetMapping(value="/questionnaires/{id}")
    public QuestionnaireDetail GetOne(@PathVariable Long id) throws NotFoundException {

        Questionnaire toReturn = HibernateAdapter.getById(Questionnaire.class, id);

        if(toReturn == null)
            throw new NotFoundException("Not found questionnare with id:" + id );

        List<QuestionnaireTerm> questionnaireTerms = HibernateAdapter.getAll(QuestionnaireTerm.class);

        return new QuestionnaireDetail(toReturn);
    }


    @PostMapping(value="/questionnaires")
    public QuestionnaireDetail Post(@RequestBody AddQuestionnaireView addQuestionnaireView){

        Questionnaire newQuestionnaire = new Questionnaire();

        if(addQuestionnaireView.getTeacher_id() == null) addQuestionnaireView.setTeacher_id(1l);

        newQuestionnaire.setExpirationDate(addQuestionnaireView.getExpirationDate());
        newQuestionnaire.setLabel(addQuestionnaireView.getLabel());
        newQuestionnaire.getTeacher().setId(addQuestionnaireView.getTeacher_id());


        HibernateAdapter.save(newQuestionnaire);

        List<Term> allTerms = HibernateAdapter.getAll(Term.class);

        for( Term term : allTerms){
            if(addQuestionnaireView.getAvailableTerms().contains(term.getId())) {
                QuestionnaireTerm qt = new  QuestionnaireTerm(newQuestionnaire, term);
                HibernateAdapter.save(qt);
            }
        }

        return new QuestionnaireDetail(newQuestionnaire);
    }

    @PutMapping(value="/questionnaires/{id}")
    public Questionnaire Put(@PathVariable Long id,@RequestBody Questionnaire questionnaire) throws Exception{

        System.out.println("Enter put");

        Questionnaire toReturn = questionnaires
                .stream()
                .filter( q -> q.getId() != null && q.getId().equals(id))
                .findFirst()
                .orElse(null);
        System.out.println(toReturn);

        if(toReturn == null)
            throw new NotFoundException("Not found questionnare with id:" + id );

        toReturn.setLabel( questionnaire.getLabel());

        return toReturn;
    }

    @DeleteMapping(value="/questionnaires/{id}")
    public Questionnaire Delete(@PathVariable Long id) throws Exception{

        Questionnaire toReturn = questionnaires
                .stream()
                .filter( q -> q.getId() != null && q.getId().equals( id))
                .findFirst()
                .orElse(null);

        if(toReturn == null)
            throw new NotFoundException("questionnaire with id=" + id);

        questionnaires.remove(toReturn);
        return toReturn;
    }
    
}
