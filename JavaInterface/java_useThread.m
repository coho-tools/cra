function java_useThread(ind)
  assert(ind>0 && ind<=cra_cfg('get','javaThreads'))
  cra_cfg('set','currThread',ind);
end
