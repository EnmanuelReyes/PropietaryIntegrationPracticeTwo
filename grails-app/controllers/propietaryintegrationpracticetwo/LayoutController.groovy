package propietaryintegrationpracticetwo

import grails.web.mime.MimeType
import org.w3c.dom.Document

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
class LayoutController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def export() {
        def l = Layout.get(params.id)

        InputStream contentStream
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // Raiz
            Document doc = docBuilder.newDocument();

            org.w3c.dom.Element rootElement = doc.createElement("Layout");
            doc.appendChild(rootElement);

            // Elemento id
            org.w3c.dom.Element id = doc.createElement("Id");
            id.appendChild(doc.createTextNode(l.id.toString()))
            rootElement.appendChild(id);

            // Elemento students
            org.w3c.dom.Element students = doc.createElement("Estudiantes")
            rootElement.appendChild(students);

            for(Student s : l.students) {
                org.w3c.dom.Element student = doc.createElement("Estudiante")

                org.w3c.dom.Element matricula = doc.createElement("Matricula");
                matricula.appendChild(doc.createTextNode(s.matricula));
                student.appendChild(matricula);

                org.w3c.dom.Element identificacion = doc.createElement("Identificacion");
                identificacion.appendChild(doc.createTextNode(s.identificacion));
                student.appendChild(identificacion);

                org.w3c.dom.Element tipoIdentificacion = doc.createElement("TipoIdentificacion");
                tipoIdentificacion.appendChild(doc.createTextNode(s.tipoIdentificacion.toString()));
                student.appendChild(tipoIdentificacion);

                org.w3c.dom.Element carrera = doc.createElement("Carrera");
                carrera.appendChild(doc.createTextNode(s.carrera));
                student.appendChild(carrera);

                org.w3c.dom.Element montoAprobado = doc.createElement("MontoAprobado");
                montoAprobado.appendChild(doc.createTextNode(s.montoAprobado.toString()));
                student.appendChild(montoAprobado);

                org.w3c.dom.Element limiteCredito = doc.createElement("LimiteCredito");
                limiteCredito.appendChild(doc.createTextNode(s.limiteCredito.toString()));
                student.appendChild(limiteCredito);


                students.appendChild(student)
            }



            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource fuenteDato = new DOMSource(doc);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            StreamResult resultado = new StreamResult(outputStream);



            // Si quisieramos sacar la salida a la consola para "pruebas"
            // StreamResult resultado = new StreamResult(System.out);
            transformer.transform(fuenteDato, resultado);
            response.setHeader "Content-disposition", "attachment; filename=Layout.xml"
            response.setContentType(MimeType.XML.name)
//            contentStream = new ByteArrayInputStream(outputStream)
//            response.outputStream << contentStream
            response.outputStream << outputStream
            webRequest.renderView = false
        } finally {

        }
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond Layout.list(params), model:[layoutCount: Layout.count()]
    }

    def show(Layout layout) {
        respond layout
    }
    def upload() {
    }

    def uploadFile() {
        def f = request.getFile('filelayout')
        if (f.empty) {
            flash.message = 'file cannot be empty'
            render(view: 'upload')
            return
        }
        File file = f.part.fileItem.tempFile;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);

        def l = new Layout()

        doc.getDocumentElement().normalize();

        System.out.println("Elemento raiz :" + doc.getDocumentElement().getNodeName());
        def layoutId = doc.getDocumentElement().getElementsByTagName('Id').item(0).getTextContent();
        l.id = layoutId as Integer
        l.students = new HashSet<>()

        NodeList nList = doc.getElementsByTagName("Estudiante");

        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) nNode;
                def matricula = eElement.getElementsByTagName("Matricula").item(0).getTextContent()
                def identificacion = eElement.getElementsByTagName("Identificacion").item(0).getTextContent()
                def tipoIdentificacion = eElement.getElementsByTagName("TipoIdentificacion").item(0).getTextContent()
                def carrera = eElement.getElementsByTagName("Carrera").item(0).getTextContent()
                def montoAprobado = eElement.getElementsByTagName("MontoAprobado").item(0).getTextContent()
                def limiteCredito = eElement.getElementsByTagName("LimiteCredito").item(0).getTextContent()
                def student = new Student(matricula: matricula, identificacion: identificacion,
                        tipoIdentificacion: tipoIdentificacion as Integer, carrera:carrera,
                        montoAprobado: montoAprobado as Double, limiteCredito: limiteCredito as Integer)
                HashSet hs = new HashSet()
                hs.add(l)
                student.layouts = hs
                l.students << student

            }
        }
        save(l)

        render(view: 'index')
    }

    def create() {
        respond new Layout(params)
    }

    @Transactional
    def save(Layout layout) {
        if (layout == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (layout.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond layout.errors, view:'create'
            return
        }

        layout.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'layout.label', default: 'Layout'), layout.id])
                redirect layout
            }
            '*' { respond layout, [status: CREATED] }
        }
    }

    def edit(Layout layout) {
        respond layout
    }

    @Transactional
    def update(Layout layout) {
        if (layout == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (layout.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond layout.errors, view:'edit'
            return
        }

        layout.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'layout.label', default: 'Layout'), layout.id])
                redirect layout
            }
            '*'{ respond layout, [status: OK] }
        }
    }

    @Transactional
    def delete(Layout layout) {

        if (layout == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        layout.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'layout.label', default: 'Layout'), layout.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'layout.label', default: 'Layout'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
