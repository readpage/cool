import{i as r}from"./vue-demi-3c7ae989.js";import{aj as p,e as l,ai as u}from"./@vue-310af8a3.js";/*! Version: v0.0.0 *//*!
  * pinia v2.1.4
  * (c) 2023 Eduardo San Martin Morote
  * @license MIT
  */const f=Symbol();var o;(function(t){t.direct="direct",t.patchObject="patch object",t.patchFunction="patch function"})(o||(o={}));function _(){const t=p(!0),s=t.run(()=>l({}));let i=[],c=[];const a=u({install(e){a._a=e,e.provide(f,a),e.config.globalProperties.$pinia=a,c.forEach(n=>i.push(n)),c=[]},use(e){return!this._a&&!r?c.push(e):i.push(e),this},_p:i,_a:null,_e:t,_s:new Map,state:s});return a}export{_ as c};
