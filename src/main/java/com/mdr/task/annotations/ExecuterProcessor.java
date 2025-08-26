package com.mdr.task.annotations;

public class ExecuterProcessor {
    
}
// import javax.annotation.processing.*;
//     import javax.lang.model.element.*;
//     import javax.tools.Diagnostic;

//     @SupportedAnnotationTypes("your.package.EnforceSignature") // Replace with your annotation's fully qualified name
//     @SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_8) // Adjust Java version
//     public class SignatureEnforcerProcessor extends AbstractProcessor {

//         @Override
//         public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
//             for (TypeElement annotation : annotations) {
//                 for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
//                     if (element.getKind() == ElementKind.METHOD) {
//                         ExecutableElement method = (ExecutableElement) element;
//                         EnforceSignature enforceSignature = method.getAnnotation(EnforceSignature.class);

//                         // Perform signature validation based on enforceSignature values
//                         // Example: Check return type
//                         if (!method.getReturnType().toString().equals(enforceSignature.returnType().getName())) {
//                             processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
//                                     "Method " + method.getSimpleName() + " has incorrect return type.", method);
//                         }
//                         // Add checks for parameter types, etc.
//                     }
//                 }
//             }
//             return true;
//         }
//     }