<div>
  <#if description??>
    <p><b>Mitgliedsgemeinden:</b> ${description}</p>
  </#if>
  <p>Nutzen Sie die folgenden Links um zu den Bauleitplanungs-Seiten zu gelangen:</p>
  <span class="link-list">
    <#list links as link>
      <div>
        <a href="${link.url}" class="icon" target="_blank" style="display: inline-block;"><span class="ic-ic-arrow"></span><span class="text">${link.text}</span></a>
      </div>
    </#list>
  </span>
</div>
