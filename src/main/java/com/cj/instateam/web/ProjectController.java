package com.cj.instateam.web;

import com.cj.instateam.model.Collaborator;
import com.cj.instateam.model.Project;
import com.cj.instateam.model.Role;
import com.cj.instateam.service.CollaboratorService;
import com.cj.instateam.service.ProjectService;
import com.cj.instateam.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
public class ProjectController {
    @Autowired
    private ProjectService projectService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private CollaboratorService collaboratorService;

    @RequestMapping(value = {"/", "/projects"}, method = RequestMethod.GET)
    public String listProjects(Model model) {
        List<Project> projects = projectService.findAll();
        model.addAttribute("projects", projects);
        return "index";
    }

    @RequestMapping(value = "/project-detail/{id}", method = RequestMethod.GET)
    public String viewProjectDetail(@PathVariable int id, ModelMap model) {
        Project project = projectService.findById(id);
        model.put("project", project);
        return "project_detail";
    }

    @RequestMapping(value = "/add_project", method = RequestMethod.GET)
    public String displayAddProjectForm(ModelMap model) {
        List<Role> roles = roleService.findAll();
        List<Collaborator> collaborators = collaboratorService.findAll();
        model.put("roles", roles);
        model.put("collaborators", collaborators);
        return "add_project";
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String addProject(@RequestParam(value = "name") String name,
                             @RequestParam(value = "description") String description,
                             @RequestParam(value = "status") String status,
                             @RequestParam(value = "roles_needed", required = false) List<String> rolesNeededIds,
                             ModelMap model) {
        List<Role> rolesNeeded = new ArrayList<>();
        for (String roleId : rolesNeededIds) {
            rolesNeeded.add(roleService.findById(Integer.parseInt(roleId)));
        }
        Project project = new Project()
                              .setName(name)
                              .setDescription(description)
                              .setStatus(status)
                              .setRolesNeeded(rolesNeeded);
        projectService.save(project); // TODO:  CJ wrap this in try-catch block.
        return "redirect:/";
    }

    @RequestMapping(value = "/edit_project/{id}", method = RequestMethod.GET)
    public String viewEditProject(@PathVariable int id, ModelMap model) {
        if (! model.containsAttribute("project")) {
            Project project = projectService.findById(id);
            model.put("project", project);
        }
        List<Role> roles = roleService.findAll();
        //model.put("project", project);
        model.put("roles", roles);
        return "edit_project";
    }

    @RequestMapping(value = "/edit_project/{id}", method = RequestMethod.POST)
    public String editProject(@Valid Project project, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("project", project);
            return "redirect:/edit_project/{id}";
        }

        Project newProject = new Project().setId(project.getId())
                                          .setName(project.getName())
                                          .setDescription(project.getDescription())
                                          .setStatus(project.getStatus())
                                          .setRolesNeeded(project.getRolesNeeded());
        projectService.save(project);
        return "redirect:/project-detail/{id}";
    }

    @RequestMapping(value = "/project_collaborators/{projectId}", method = RequestMethod.GET)
    public String editProjectCollaborators(@PathVariable(value = "projectId") String projectId,
                                           ModelMap model) {
        Project project = projectService.findById(Integer.parseInt(projectId));
        List<Role> roles = roleService.findAll();
        List<Collaborator> collaborators = collaboratorService.findAll();
        model.put("project", project);
        model.put("roles", roles);
        model.put("collaborators", collaborators);

        return "project_collaborators";
    }

    @RequestMapping(value = "/project-detail/{id}", method = RequestMethod.POST)
    public String editCollaboratorsAndRoles(@Valid Project project, BindingResult result) {
        List<Collaborator> collaborators = project.getCollaborators(); // get collaborators from project parameter
        Project origProject = projectService.findById(project.getId()); // get project that persists in db
        // not updating roles here, so no need to set project's rolesNeeded field.  Roles are adjusted when editing project
        origProject.setCollaborators(collaborators);
        projectService.save(origProject);
        return "redirect:/project-detail/{id}";
    }

    /*private Map<Role, List<Collaborator>> mapRolesAndCollaborators(Project project) {
        // get all project roles
        List<Role> roles = project.getRolesNeeded();
        // get all project collaborators
        List<Collaborator> collaborators = project.getCollaborators();
        // add each role to map
        Map<Role, List<Collaborator>> map = new HashMap<>();
        for (Role role : roles) {
            List<Collaborator> collsWithRoleId = collaborators.stream().filter(c -> c.getId() == role.getId()).collect(Collectors.toList());
            map.put(role, collsWithRoleId);
            if (collsWithRoleId.size() > 1) {
                map.put(role, collsWithRoleId.get(0));
                // remove collaborator from project collaborators once it is added to the map so that collaborators are not added twice.
                collaborators.remove(collsWithRoleId.get(0));
            } else {
                map.put(role, collsWithRoleId.get(0));
            }

        }
        return map;
        // for each role search thru collaborators and remove the one that matches the role id...add collaborator to map

        // return map
    }*/

}
