/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2016 wcm.io
 * %%
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
 * #L%
 */
package io.wcm.devops.conga.generator.plugins.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import com.google.common.collect.ImmutableList;

import io.wcm.devops.conga.generator.ContextPropertiesBuilder;
import io.wcm.devops.conga.generator.GeneratorException;
import io.wcm.devops.conga.generator.spi.export.NodeModelExportPlugin;
import io.wcm.devops.conga.generator.spi.export.context.ExportNodeRoleData;
import io.wcm.devops.conga.generator.spi.export.context.ExportNodeRoleTenantData;
import io.wcm.devops.conga.generator.spi.export.context.NodeModelExportContext;
import io.wcm.devops.conga.generator.util.FileUtil;

/**
 * Exports model information for each node in YAML format.
 * This is useful e.g. for integration with Ansible.
 */
public class YamlNodeModelExport implements NodeModelExportPlugin {

  /**
   * Plugin name
   */
  public static final String NAME = "yaml";

  private static final String MODEL_FILE = "model.yaml";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public void export(NodeModelExportContext context) {

    // generate YAML data
    List<Map<String, Object>> roles = new ArrayList<>();
    for (ExportNodeRoleData roleData : context.getRoleData()) {
      addRole(roles, roleData, context);
    }

    Map<String, Object> modelMap = new LinkedHashMap<>();
    modelMap.put("roles", roles);

    // save YAML file
    save(modelMap, context);
  }

  private void addRole(List<Map<String, Object>> modelList, ExportNodeRoleData roleData, NodeModelExportContext context) {
    String nodeDirPath = FileUtil.getCanonicalPath(context.getNodeDir());

    Map<String, Object> roleMap = new LinkedHashMap<>();
    roleMap.put("role", roleData.getRole());
    if (StringUtils.isNotEmpty(roleData.getRoleVariant())) {
      roleMap.put("variant", roleData.getRoleVariant());
    }

    roleMap.put("files", roleData.getFiles().stream()
        .map(item -> {
          Map<String, Object> itemMap = new LinkedHashMap<String, Object>();
          itemMap.put("path", cleanupFileName(item.getFileContext().getCanonicalPath(), nodeDirPath));
          if (!item.getPostProcessors().isEmpty()) {
            itemMap.put("postProcessors", ImmutableList.copyOf(item.getPostProcessors()));
          }
          Map<String, Object> modelOptions = item.getFileContext().getModelOptions();
          if (modelOptions != null) {
            for (Map.Entry<String, Object> entry : modelOptions.entrySet()) {
              if (!itemMap.containsKey(entry.getKey())) {
                itemMap.put(entry.getKey(), entry.getValue());
              }
            }
          }
          return itemMap;
        })
        .collect(Collectors.toList()));

    roleMap.put("config", cleanupConfig(roleData.getConfig()));

    addTenants(roleMap, roleData);

    modelList.add(roleMap);
  }

  private void addTenants(Map<String, Object> roleMap, ExportNodeRoleData roleData) {
    List<Map<String, Object>> tenants = new ArrayList<>();

    if (roleData.getTenantData() != null) {
      for (ExportNodeRoleTenantData tenantData : roleData.getTenantData()) {
        addTenant(tenants, tenantData);
      }
    }

    if (!tenants.isEmpty()) {
      roleMap.put("tenants", tenants);
    }
  }

  private void addTenant(List<Map<String, Object>> tenants, ExportNodeRoleTenantData tenantData) {
    Map<String, Object> tenantMap = new LinkedHashMap<>();

    tenantMap.put("tenant", tenantData.getTenant());
    if (!tenantData.getRoles().isEmpty()) {
      tenantMap.put("roles", tenantData.getRoles());
    }
    tenantMap.put("config", cleanupConfig(tenantData.getConfig()));

    tenants.add(tenantMap);
  }

  private void save(Map<String, Object> modelMap, NodeModelExportContext context) {
    File file = new File(context.getNodeDir(), MODEL_FILE);
    try (FileOutputStream os = new FileOutputStream(file);
        OutputStreamWriter writer = new OutputStreamWriter(os, CharEncoding.UTF_8)) {
      new Yaml().dump(modelMap, writer);
    }
    catch (Throwable ex) {
      throw new GeneratorException("Unable to write model file: " + FileUtil.getCanonicalPath(file), ex);
    }
  }

  private Map<String, Object> cleanupConfig(Map<String, Object> config) {
    return ContextPropertiesBuilder.removeContextVariables(config);
  }

  private String cleanupFileName(String fileName, String basePath) {
    String relativePath = StringUtils.substring(fileName, basePath.length() + 1);
    return StringUtils.replace(relativePath, File.separator, "/");
  }

}