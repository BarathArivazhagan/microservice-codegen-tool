package com.barath.codegen.app.generator;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.barath.codegen.app.constant.CodeGenerationConstants;
import com.barath.codegen.app.model.RequestModel;
import com.barath.codegen.app.service.SwaggerCodegenService;
import com.barath.codegen.app.util.PropertyResolverUtility;
import com.barath.codegen.app.util.SwaggerUtils;

import io.spring.initializr.generator.ProjectGenerator;
import io.spring.initializr.generator.ProjectRequest;

@Component
public class CustomProjectGenerator extends ProjectGenerator {
	
	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
		
	@Autowired
	private SwaggerCodegenService swaggerCodeGen;
	
	@Autowired
	private EntityGenerator entityGenerator;
	
	@Autowired
	private ControllerGenerator controllerGenerator;
	
	@Autowired
	private ServiceGenerator serviceGenerator;
	
	@Autowired
	private RepositoryGenerator repositoryGenerator;
	
	public File generateCustomProjectStructure(RequestModel request) {
		
		
		Map<String,Object> model=new HashMap<>();
		model.put(CodeGenerationConstants.APPLICATION_NAME, request.getApplicationName());
		model.put(CodeGenerationConstants.ARTIFACT_ID, request.getArtifactId());
		model.put(CodeGenerationConstants.PACKAGAING, request.getPackaging());		
		model.put(CodeGenerationConstants.BASE_DIR, request.getBaseDir());
		model.put(CodeGenerationConstants.VERSION,"1.0.0-SNAPSHOT");
		if(isMavenBuild(request)) {
			model.put("mavenBuild", true);
		}	
		File dir=generateProjectStructure(request);
		if(logger.isInfoEnabled()) { logger.info("Generated base sturcture {}",dir); }		
		generateTemplateCodegen(dir,request);
		this.swaggerCodeGen.generateSwaggerCodegen(dir,request);		
		generateDockerFile(dir, model);	
		return dir;
	}
	
	
	
	@Override
	protected Map<String, Object> resolveModel(ProjectRequest originalRequest) {
		
		Map<String,Object> model=super.resolveModel(originalRequest);
		if(isMavenBuild(originalRequest)) {
			model.put("mavenBuild", true);
		}	
		model.put("version","0.0.1-SNAPSHOT");
		if(logger.isInfoEnabled()) { logger.info("POM MODEL {}",model); }
		return model;
	}
	
	
	
	@SuppressWarnings("unchecked")
	private void generateApplicationProperties(File dir,Map<String,Object> model) {
		
		String baseDir=(String) model.get("baseDir");
		Map<String,Object> appProperties=(Map<String, Object>) model.get("properties");
		Map<String,Object> applicationProperties=new HashMap<>();
		if( appProperties !=null &&  !appProperties.isEmpty()){
			
			applicationProperties.put("properties", appProperties.entrySet());
		}	
	
		System.out.println("application properties "+applicationProperties);
		File applicationFile=new File(dir,baseDir+"/src/main/resources/");
		write(new File(applicationFile,"application.properties"),"application", applicationProperties);
	}





	private void generateTemplateCodegen(File dir, RequestModel request) {
		
		String templateType=PropertyResolverUtility.resolveTemplateType(request.getTemplateType());
		System.out.println("template type "+templateType);
		switch(templateType) {
				
		    case "CRUD": generateCRUDTemplate(dir,request);break;
		    case "JPA": generateJPATemplate(dir,request);break;
		    case "RABBITMQ": generateRabbitMqTemplate(dir,request);break;
		    default : generateCRUDTemplate(dir, request);
		}
	}



	
	
	
	private void generateJPATemplate(File dir, RequestModel request) {
		generateCRUDTemplate(dir, request);
		
	}



	private void generateRabbitMqTemplate(File dir, RequestModel request) {
		// TODO Auto-generated method stub
		
	}



	protected void generateDockerFile(File dir, Map<String,Object> model){
			
	    write(new File(new File(dir,(String)model.get("baseDir")),"Dockerfile"), "Dockerfile", model);
	   
	   
	}
	
	private static boolean isMavenBuild(ProjectRequest request) {
		return "maven-project".equals(request.getType());
	}
	

	protected void generateCRUDTemplate(File dir, RequestModel request){
		
		Map<String,Object> swaggerMap=SwaggerUtils.getSwaggerMap(request.getSwaggerJson());		                
		swaggerMap.put("packageName", request.getPackageName());	
		swaggerMap.put("artifactId", request.getArtifactId());		
		swaggerMap.put("baseDir", request.getBaseDir());	
		List<Map<String, Object>> entities=generateEntities(dir,swaggerMap);
		List<Map<String, Object>> repositories=generateRepositories(dir, entities);
		List<Map<String, Object>> services=generateServices(dir, repositories);
		List<Map<String, Object>> controllers=generateControllers(dir, services);
		generateApplicationProperties(dir,swaggerMap);
	}
	
	public List<Map<String, Object>> generateEntities(File target,Map<String,Object> input){
		
		List<Map<String, Object>> entities=entityGenerator.buildEntities(target, input);
		entityGenerator.writeEntities(target, entities);
		return entities;
	}
	
	public List<Map<String, Object>> generateServices(File target,List<Map<String, Object>> input){
			
		List<Map<String, Object>> services=serviceGenerator.buildServices(target, input);
		serviceGenerator.writeServices(target, services);
		return services;
	}
	
	public List<Map<String, Object>> generateRepositories(File target,List<Map<String, Object>> input){
		
		List<Map<String, Object>> repositories= repositoryGenerator.buildRepositories(target, input);
		repositoryGenerator.writeRepositories(target, repositories);
		return repositories;
	}
	
	public List<Map<String, Object>> generateControllers(File target,List<Map<String, Object>> input){
		
		List<Map<String, Object>> controllers= controllerGenerator.buildControllers(target, input);
		controllerGenerator.writeControllers(target, controllers);
		return controllers;
	}
	
	
	
	
	

}
