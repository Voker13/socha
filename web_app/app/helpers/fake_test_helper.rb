module FakeTestHelper
  def image_for_state(state)
    image_for_value(states, state)
  end

  def image_for_action(action)
    image_for_value(actions, action)
  end

  def is_action?(action)
    is_value?(actions,action)
  end

  def is_state?(state)
    is_value?(states,state)
  end

  def states
   {'true' => 'state/ok.png',
    'false' => 'state/warning.png',
    'finished' => 'state/ok.png',
    'error' => 'state/attention.png',
    'attention' => 'state/attention.png',
    'ready' => 'state/info.png',
    'idle' => 'ui/spinner.gif'}
  end

  def actions
   {'show' => 'actions/show.png',
    'delete' => 'actions/delete.png',
    'edit' => 'actions/edit.png',
    'refresh' => 'actions/undo.gif'}
  end
  
  def msg_for_state(state)
    I18n.t('messages.states.'+state)
  end

 protected
  def image_for_value(values, val)
    image_tag values[val.to_s]
  end

  def is_value?(values,value)
    values.keys.include? value
  end

end