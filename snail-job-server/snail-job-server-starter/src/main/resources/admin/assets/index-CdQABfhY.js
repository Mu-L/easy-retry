import{_ as B,u as I,a as $,b as W}from"./table-Bdl3-G-Y.js";import{d as S,U as z,V as M,o as R,c as U,w as k,f as t,h as e,$ as a,s as j,b as C,ad as v,cx as D,N as y,g as w,Q as T,bd as F,I as O,ae as V}from"./index-CHgAHQIl.js";import{d as K}from"./dashboard-CGSNSQXZ.js";import{_ as q}from"./search-form.vue_vue_type_script_setup_true_lang-BfZ4by1v.js";import{_ as A}from"./select-group.vue_vue_type_script_setup_true_lang-fZ9yr9an.js";import{_ as E}from"./Grid-DHGKk79k.js";import"./FormItem-B3tfy17D.js";import"./Space-CeVCCXQI.js";import"./round-search-KK91k1IY.js";import"./form-BuwwOwyC.js";import"./group-BUALrrKR.js";const G=S({name:"PodsSearch",__name:"pods-search",props:{model:{required:!0},modelModifiers:{}},emits:z(["reset","search"],["update:model"]),setup(d,{emit:x}){const c=x,l=M(d,"model");function h(){c("reset")}function i(){c("search")}return(b,f)=>{const u=B,N=q;return R(),U(N,{model:l.value,onSearch:i,onReset:h},{default:k(()=>[t(u,{span:"24 s:12 m:6",label:e(a)("page.pods.groupName"),path:"groupName",class:"pr-24px"},{default:k(()=>[t(A,{value:l.value.groupName,"onUpdate:value":f[0]||(f[0]=p=>l.value.groupName=p),placeholder:e(a)("page.pods.form.groupName")},null,8,["value","placeholder"])]),_:1},8,["label"])]),_:1},8,["model"])}}}),H={class:"min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto"};function P(d){return typeof d=="function"||Object.prototype.toString.call(d)==="[object Object]"&&!V(d)}const ne=S({name:"pods",__name:"index",setup(d){const x=j(),{columns:c,columnChecks:l,data:h,getData:i,loading:b,mobilePagination:f,searchParams:u,resetSearchParams:N}=I({apiFn:K,apiParams:{page:1,size:10,groupName:null},columns:()=>[{key:"hostId",title:a("page.pods.hostId"),align:"left",resizable:!0,width:150,minWidth:150,maxWidth:200},{key:"nodeType",title:a("page.pods.nodeType"),align:"center",width:80,render:s=>{if(s.nodeType===null)return null;const o={1:"info",2:"primary"},r=a(D[s.nodeType]);return t(y,{type:o[s.nodeType]},P(r)?r:{default:()=>[r]})}},{key:"groupName",title:a("page.pods.groupName"),align:"left",width:120,resizable:!0,minWidth:120,maxWidth:200},{key:"hostIp",title:a("page.pods.hostIp"),align:"left",width:120},{key:"hostPort",title:a("page.pods.hostPort"),align:"left",width:80},{key:"consumerBuckets",title:a("page.pods.consumerBuckets"),align:"left",width:300,resizable:!0,minWidth:120,maxWidth:400,render:s=>{if(s.nodeType===null)return null;const o=g=>{var n;const m=g?(n=s.consumerBuckets)==null?void 0:n.slice(0,g):s.consumerBuckets;return m==null?void 0:m.map(_=>t(y,{type:"error",key:_,class:"m-1 justify-center"},P(_)?_:{default:()=>[_]}))},r=()=>t(y,{type:"info"},{default:()=>[s.contextPath??"/"]});return s.nodeType===1?t(T,null,[w("Path: "),r()]):t(T,null,[t("span",null,[w("Bucket: ")]),o(10),t(F,{trigger:"hover"},{trigger:()=>t(y,{type:"error"},{default:()=>[w("...")]}),default:()=>t("div",{class:"grid grid-cols-16"},[o()])})])}},{key:"updateDt",title:a("page.pods.updateDt"),align:"left",width:130}]}),{checkedRowKeys:p}=$(h,i);return(s,o)=>{const r=W,g=E,m=O;return R(),C("div",H,[t(G,{model:e(u),"onUpdate:model":o[0]||(o[0]=n=>v(u)?u.value=n:null),onReset:e(N),onSearch:e(i)},null,8,["model","onReset","onSearch"]),t(m,{title:e(a)("page.pods.title"),bordered:!1,size:"small","header-class":"view-card-header",class:"sm:flex-1-hidden card-wrapper"},{"header-extra":k(()=>[t(r,{columns:e(l),"onUpdate:columns":o[1]||(o[1]=n=>v(l)?l.value=n:null),"disabled-delete":e(p).length===0,loading:e(b),"show-add":!1,"show-delete":!1,onRefresh:e(i)},null,8,["columns","disabled-delete","loading","onRefresh"])]),default:k(()=>[t(g,{"checked-row-keys":e(p),"onUpdate:checkedRowKeys":o[2]||(o[2]=n=>v(p)?p.value=n:null),columns:e(c),data:e(h),"flex-height":!e(x).isMobile,"scroll-x":962,loading:e(b),remote:"","row-key":n=>n.hostId,pagination:e(f),class:"sm:h-full"},null,8,["checked-row-keys","columns","data","flex-height","loading","row-key","pagination"])]),_:1},8,["title"])])}}});export{ne as default};