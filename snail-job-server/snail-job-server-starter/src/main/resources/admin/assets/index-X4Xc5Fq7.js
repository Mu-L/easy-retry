import{d,dR as m,J as w,dQ as _,p as u,i as k,o as v,c as g,h,ew as x,ex as y,$ as S}from"./index-CHgAHQIl.js";import{_ as $}from"./workflow.vue_vue_type_script_setup_true_lang-BUvQogY_.js";import"./Grid-DHGKk79k.js";import"./FormItem-B3tfy17D.js";import"./Space-CeVCCXQI.js";import"./DescriptionsItem-sCQ7QkHp.js";import"./index-DBxP5q3t.js";const D=d({name:"workflow_form_edit",__name:"index",setup(R){const a=m(),l=w(),n=_(),s=u(!1),r=String(l.query.id),o=u({}),i=async()=>{s.value=!0;const{data:t,error:e}=await x(r);e||(o.value=t),s.value=!1};k(()=>{a.clear(),a.setType(0),a.setId(r),i()});const c=async()=>{var e;const{error:t}=await y(o.value);t||((e=window.$message)==null||e.info(S("common.updateSuccess")),n.push({path:"/workflow/task"}))},p=()=>{n.push("/workflow/task")};return(t,e)=>(v(),g(h($),{modelValue:o.value,"onUpdate:modelValue":e[0]||(e[0]=f=>o.value=f),spinning:s.value,onSave:c,onCancel:p},null,8,["modelValue","spinning"]))}});export{D as default};