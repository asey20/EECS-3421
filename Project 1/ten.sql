select cid, ((select min(s.cost) from yrb_shipping s where s.weight > sum(b.weight * p.qnty)) + sum(o.price * p.qnty)) cost
from

(select cid, max(cost) cost
from
(select t.cid, t.day, max(t.cost) cost 
from
(select p.cid, p.club, cast(p.when as date) as day,sum(o.price * p.qnty) cost 
from yrb_purchase p, yrb_book b, yrb_offer o
where b.title = p.title AND b.year = p.year AND p.title = o.title AND p.year = o.year AND p.club = o.club 
group by p.cid, p.when, p.club
order by p.cid) t, yrb_purchase p
where t.cid = p.cid AND t.club = p.club
group by t.cid, t.day
order by t.cid)
group by cid)
group by cid
;
